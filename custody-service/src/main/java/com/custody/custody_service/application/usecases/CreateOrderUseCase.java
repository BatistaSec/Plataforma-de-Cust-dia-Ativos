package com.custody.custody_service.application.usecases;

import com.custody.custody_service.application.dto.CreateOrderInput;
import com.custody.custody_service.application.dto.OrderOutput;
import com.custody.custody_service.domain.enums.MovementType;
import com.custody.custody_service.domain.enums.OrderStatus;
import com.custody.custody_service.domain.events.OrderEvent;
import com.custody.custody_service.domain.exceptions.InsufficientBalanceException;
import com.custody.custody_service.domain.exceptions.ResourceNotFoundException;
import com.custody.custody_service.domain.models.Asset;
import com.custody.custody_service.domain.models.AssetMovement;
import com.custody.custody_service.domain.models.CustodyOrder;
import com.custody.custody_service.domain.models.Portfolio;
import com.custody.custody_service.domain.models.PortfolioAsset;
import com.custody.custody_service.domain.repositories.AssetMovementRepository;
import com.custody.custody_service.domain.repositories.AssetRepository;
import com.custody.custody_service.domain.repositories.CustodyOrderRepository;
import com.custody.custody_service.domain.repositories.PortfolioRepository;
import com.custody.custody_service.infrastructure.mappers.AssetMapper;
import com.custody.custody_service.infrastructure.mappers.PortfolioMapper;
import com.custody.custody_service.infrastructure.persistence.entities.PortfolioAssetEntity;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataPortfolioAssetRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class CreateOrderUseCase {

    private final CustodyOrderRepository orderRepository;
    private final PortfolioRepository portfolioRepository;
    private final AssetRepository assetRepository;
    private final AssetMovementRepository assetMovementRepository;
    private final SpringDataPortfolioAssetRepository portfolioAssetRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${kafka.topics.custody-orders:custody-orders-topic}")
    private String ordersTopic;

    public CreateOrderUseCase(CustodyOrderRepository orderRepository,
                               PortfolioRepository portfolioRepository,
                               AssetRepository assetRepository,
                               AssetMovementRepository assetMovementRepository,
                               SpringDataPortfolioAssetRepository portfolioAssetRepository,
                               KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.portfolioRepository = portfolioRepository;
        this.assetRepository = assetRepository;
        this.assetMovementRepository = assetMovementRepository;
        this.portfolioAssetRepository = portfolioAssetRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public OrderOutput execute(CreateOrderInput input) {
        UUID idempotencyKey = input.getIdempotencyKey() != null
                ? input.getIdempotencyKey()
                : UUID.randomUUID();

        // (a) Idempotency check — if order already exists with this key, return it (200 OK)
        Optional<CustodyOrder> existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
        if (existingOrder.isPresent()) {
            CustodyOrder order = existingOrder.get();
            return new OrderOutput(
                    order.getId(),
                    order.getPortfolio().getId(),
                    order.getAsset().getId(),
                    order.getType(),
                    order.getQuantity(),
                    order.getStatus(),
                    order.getCreatedAt(),
                    order.getIdempotencyKey(),
                    true
            );
        }

        // Load portfolio and asset
        Portfolio portfolio = portfolioRepository.findById(input.getPortfolioId())
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio não encontrado"));

        Asset asset = assetRepository.findById(input.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset não encontrado"));

        // (b) Resolve existing PortfolioAsset position (load for optimistic lock)
        Optional<PortfolioAssetEntity> existingPosition = portfolioAssetRepository
                .findByPortfolioIdAndAssetId(input.getPortfolioId(), input.getAssetId());

        PortfolioAssetEntity positionEntity;

        if (existingPosition.isPresent()) {
            positionEntity = existingPosition.get();

            // Convert to domain for debit/credit validation
            PortfolioAsset domainAsset = new PortfolioAsset(
                    positionEntity.getId(),
                    portfolio,
                    asset,
                    positionEntity.getQuantity(),
                    positionEntity.getVersion()
            );

            // (c) SELL: debit with balance check; (d) BUY: credit
            if (input.getType() == com.custody.custody_service.domain.enums.OrderType.SELL) {
                domainAsset.debit(input.getQuantity());
            } else {
                domainAsset.credit(input.getQuantity());
            }

            positionEntity.setQuantity(domainAsset.getQuantity());
        } else {
            // No existing position
            if (input.getType() == com.custody.custody_service.domain.enums.OrderType.SELL) {
                throw new InsufficientBalanceException(BigDecimal.ZERO, input.getQuantity());
            }
            // BUY: create new position
            positionEntity = new PortfolioAssetEntity();
            positionEntity.setPortfolio(PortfolioMapper.toEntity(portfolio));
            positionEntity.setAsset(AssetMapper.toEntity(asset));
            positionEntity.setQuantity(input.getQuantity());
        }

        // (f) Save CustodyOrder with idempotencyKey
        CustodyOrder order = new CustodyOrder(
                portfolio, asset, input.getType(), input.getQuantity(),
                OrderStatus.PENDING, idempotencyKey
        );
        CustodyOrder savedOrder = orderRepository.save(order);

        // (g) Create and save AssetMovement (DEBIT for SELL, CREDIT for BUY)
        MovementType movementType = input.getType() == com.custody.custody_service.domain.enums.OrderType.SELL
                ? MovementType.DEBIT
                : MovementType.CREDIT;

        AssetMovement movement = new AssetMovement(
                null,
                portfolio.getId(),
                asset.getId(),
                movementType,
                input.getQuantity(),
                savedOrder.getId(),
                LocalDateTime.now()
        );
        assetMovementRepository.save(movement);

        // (h) Save PortfolioAsset (triggers @Version optimistic lock check)
        portfolioAssetRepository.save(positionEntity);

        // (i) Publish OrderEvent (only for new orders, not replays)
        OrderEvent event = new OrderEvent(
                savedOrder.getId(),
                portfolio.getId(),
                asset.getId(),
                savedOrder.getType(),
                savedOrder.getQuantity(),
                savedOrder.getStatus()
        );
        kafkaTemplate.send(ordersTopic, savedOrder.getId().toString(), event);

        return new OrderOutput(
                savedOrder.getId(),
                portfolio.getId(),
                asset.getId(),
                savedOrder.getType(),
                savedOrder.getQuantity(),
                savedOrder.getStatus(),
                savedOrder.getCreatedAt(),
                savedOrder.getIdempotencyKey(),
                false
        );
    }
}
