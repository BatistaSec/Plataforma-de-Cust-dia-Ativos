package com.custody.custody_service.application.usecases;

import com.custody.custody_service.application.dto.CreateOrderInput;
import com.custody.custody_service.application.dto.OrderOutput;
import com.custody.custody_service.domain.enums.EntryType;
import com.custody.custody_service.domain.enums.MovementType;
import com.custody.custody_service.domain.enums.OrderStatus;
import com.custody.custody_service.domain.enums.OrderType;
import com.custody.custody_service.domain.enums.TransactionStatus;
import com.custody.custody_service.domain.enums.TransactionType;
import com.custody.custody_service.domain.events.OrderEvent;
import com.custody.custody_service.domain.exceptions.IdempotencyKeyConflictException;
import com.custody.custody_service.domain.exceptions.InsufficientBalanceException;
import com.custody.custody_service.domain.exceptions.InvalidScaleException;
import com.custody.custody_service.domain.exceptions.ResourceNotFoundException;
import com.custody.custody_service.domain.models.Asset;
import com.custody.custody_service.domain.models.AssetMovement;
import com.custody.custody_service.domain.models.CustodyOrder;
import com.custody.custody_service.domain.models.IdempotencyRecord;
import com.custody.custody_service.domain.models.LedgerEntry;
import com.custody.custody_service.domain.models.LedgerTransaction;
import com.custody.custody_service.domain.models.Portfolio;
import com.custody.custody_service.domain.models.PortfolioAsset;
import com.custody.custody_service.domain.repositories.AssetMovementRepository;
import com.custody.custody_service.domain.repositories.AssetRepository;
import com.custody.custody_service.domain.repositories.CustodyOrderRepository;
import com.custody.custody_service.domain.repositories.IdempotencyRecordRepository;
import com.custody.custody_service.domain.repositories.LedgerEntryRepository;
import com.custody.custody_service.domain.repositories.LedgerTransactionRepository;
import com.custody.custody_service.domain.repositories.PortfolioRepository;
import com.custody.custody_service.infrastructure.mappers.AssetMapper;
import com.custody.custody_service.infrastructure.mappers.PortfolioMapper;
import com.custody.custody_service.infrastructure.persistence.entities.PortfolioAssetEntity;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataPortfolioAssetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class CreateOrderUseCase {

    private final CustodyOrderRepository orderRepository;
    private final PortfolioRepository portfolioRepository;
    private final AssetRepository assetRepository;
    private final AssetMovementRepository assetMovementRepository;
    private final SpringDataPortfolioAssetRepository portfolioAssetRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.custody-orders:custody-orders-topic}")
    private String ordersTopic;

    public CreateOrderUseCase(CustodyOrderRepository orderRepository,
                               PortfolioRepository portfolioRepository,
                               AssetRepository assetRepository,
                               AssetMovementRepository assetMovementRepository,
                               SpringDataPortfolioAssetRepository portfolioAssetRepository,
                               LedgerTransactionRepository ledgerTransactionRepository,
                               LedgerEntryRepository ledgerEntryRepository,
                               IdempotencyRecordRepository idempotencyRecordRepository,
                               KafkaTemplate<String, OrderEvent> kafkaTemplate,
                               ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.portfolioRepository = portfolioRepository;
        this.assetRepository = assetRepository;
        this.assetMovementRepository = assetMovementRepository;
        this.portfolioAssetRepository = portfolioAssetRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderOutput execute(CreateOrderInput input, UUID idempotencyKey) {
        if (idempotencyKey == null) {
            idempotencyKey = UUID.randomUUID();
        }

        String requestHash = computeHash(input);

        // (a) Check idempotency record — replay or conflict
        Optional<IdempotencyRecord> existing = idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if (record.getRequestHash().equals(requestHash)) {
                return deserializeSnapshot(record.getResponseSnapshot(), true);
            } else {
                throw new IdempotencyKeyConflictException(
                    "Idempotency-Key ja utilizado com parametros diferentes");
            }
        }

        // Legacy fallback — check custody_orders.idempotency_key for backward compat
        Optional<CustodyOrder> legacyOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
        if (legacyOrder.isPresent()) {
            CustodyOrder order = legacyOrder.get();
            return new OrderOutput(
                order.getId(), order.getPortfolio().getId(), order.getAsset().getId(),
                order.getType(), order.getQuantity(), order.getStatus(),
                order.getCreatedAt(), order.getIdempotencyKey(), true
            );
        }

        // Load portfolio and asset
        Portfolio portfolio = portfolioRepository.findById(input.getPortfolioId())
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio nao encontrado"));
        Asset asset = assetRepository.findById(input.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset nao encontrado"));

        // (c) Validate scale: quantity scale must not exceed asset scale
        if (input.getQuantity().scale() > asset.getScale()) {
            throw new InvalidScaleException(asset.getScale(), input.getQuantity().scale());
        }

        // (b) Resolve existing PortfolioAsset position (load for optimistic lock)
        Optional<PortfolioAssetEntity> existingPosition = portfolioAssetRepository
                .findByPortfolioIdAndAssetId(input.getPortfolioId(), input.getAssetId());

        PortfolioAssetEntity positionEntity;

        if (existingPosition.isPresent()) {
            positionEntity = existingPosition.get();
            PortfolioAsset domainAsset = new PortfolioAsset(
                    positionEntity.getId(), portfolio, asset,
                    positionEntity.getQuantity(), positionEntity.getVersion()
            );
            if (input.getType() == OrderType.SELL) {
                domainAsset.debit(input.getQuantity());
            } else {
                domainAsset.credit(input.getQuantity());
            }
            positionEntity.setQuantity(domainAsset.getQuantity());
        } else {
            if (input.getType() == OrderType.SELL) {
                throw new InsufficientBalanceException(BigDecimal.ZERO, input.getQuantity());
            }
            positionEntity = new PortfolioAssetEntity();
            positionEntity.setPortfolio(PortfolioMapper.toEntity(portfolio));
            positionEntity.setAsset(AssetMapper.toEntity(asset));
            positionEntity.setQuantity(input.getQuantity());
        }

        // (f) Save CustodyOrder with idempotencyKey (backward compat)
        CustodyOrder order = new CustodyOrder(
                portfolio, asset, input.getType(), input.getQuantity(),
                OrderStatus.PENDING, idempotencyKey
        );
        CustodyOrder savedOrder = orderRepository.save(order);

        // (e) Create LedgerTransaction + LedgerEntry
        LedgerTransaction tx = new LedgerTransaction(
                null, savedOrder.getId(), TransactionType.ORDER,
                TransactionStatus.CONFIRMED, LocalDateTime.now()
        );
        LedgerTransaction savedTx = ledgerTransactionRepository.save(tx);

        EntryType entryType = input.getType() == OrderType.SELL ? EntryType.DEBIT : EntryType.CREDIT;
        LedgerEntry entry = new LedgerEntry(
                null, savedTx.getId(), portfolio.getId(), asset.getId(),
                entryType, input.getQuantity(), LocalDateTime.now()
        );
        ledgerEntryRepository.save(entry);

        // (f) Keep AssetMovement for backward compatibility
        MovementType movementType = input.getType() == OrderType.SELL
                ? MovementType.DEBIT : MovementType.CREDIT;
        AssetMovement movement = new AssetMovement(
                null, portfolio.getId(), asset.getId(), movementType,
                input.getQuantity(), savedOrder.getId(), LocalDateTime.now()
        );
        assetMovementRepository.save(movement);

        // (h) Save PortfolioAsset (triggers @Version optimistic lock check)
        portfolioAssetRepository.save(positionEntity);

        // (i) Publish OrderEvent
        OrderEvent event = new OrderEvent(
                savedOrder.getId(), portfolio.getId(), asset.getId(),
                savedOrder.getType(), savedOrder.getQuantity(), savedOrder.getStatus()
        );
        kafkaTemplate.send(ordersTopic, savedOrder.getId().toString(), event);

        // Build output and store idempotency record
        OrderOutput output = new OrderOutput(
                savedOrder.getId(), portfolio.getId(), asset.getId(),
                savedOrder.getType(), savedOrder.getQuantity(), savedOrder.getStatus(),
                savedOrder.getCreatedAt(), idempotencyKey, false
        );

        String snapshot = serializeSnapshot(output);
        IdempotencyRecord record = new IdempotencyRecord(
                null, idempotencyKey, requestHash, snapshot, LocalDateTime.now()
        );
        idempotencyRecordRepository.save(record);

        return output;
    }

    private String computeHash(CreateOrderInput input) {
        try {
            String data = input.getPortfolioId() + ":" + input.getAssetId() + ":"
                    + input.getType() + ":" + input.getQuantity();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular hash", e);
        }
    }

    private String serializeSnapshot(OrderOutput output) {
        try {
            return objectMapper.writeValueAsString(output);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar resposta", e);
        }
    }

    private OrderOutput deserializeSnapshot(String snapshot, boolean alreadyExisted) {
        try {
            OrderOutput parsed = objectMapper.readValue(snapshot, OrderOutput.class);
            return new OrderOutput(
                    parsed.getId(), parsed.getPortfolioId(), parsed.getAssetId(),
                    parsed.getType(), parsed.getQuantity(), parsed.getStatus(),
                    parsed.getCreatedAt(), parsed.getIdempotencyKey(), alreadyExisted
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deserializar resposta", e);
        }
    }
}
