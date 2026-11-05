package com.custody.custody_service.application.usecases;

import com.custody.custody_service.application.dto.CreateOrderInput;
import com.custody.custody_service.application.dto.OrderOutput;
import com.custody.custody_service.domain.enums.OrderStatus;
import com.custody.custody_service.domain.enums.OrderType;
import com.custody.custody_service.domain.events.OrderEvent;
import com.custody.custody_service.domain.exceptions.IdempotencyKeyConflictException;
import com.custody.custody_service.domain.exceptions.InsufficientBalanceException;
import com.custody.custody_service.domain.exceptions.ResourceNotFoundException;
import com.custody.custody_service.domain.models.Asset;
import com.custody.custody_service.domain.models.CustodyOrder;
import com.custody.custody_service.domain.models.IdempotencyRecord;
import com.custody.custody_service.domain.models.LedgerTransaction;
import com.custody.custody_service.domain.models.Portfolio;
import com.custody.custody_service.domain.repositories.AssetMovementRepository;
import com.custody.custody_service.domain.repositories.AssetRepository;
import com.custody.custody_service.domain.repositories.CustodyOrderRepository;
import com.custody.custody_service.domain.repositories.IdempotencyRecordRepository;
import com.custody.custody_service.domain.repositories.LedgerEntryRepository;
import com.custody.custody_service.domain.repositories.LedgerTransactionRepository;
import com.custody.custody_service.domain.repositories.PortfolioRepository;
import com.custody.custody_service.infrastructure.persistence.entities.PortfolioAssetEntity;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataPortfolioAssetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateOrderUseCase — unit tests with Mockito")
class CreateOrderUseCaseTest {

    @Mock private CustodyOrderRepository orderRepository;
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private AssetRepository assetRepository;
    @Mock private AssetMovementRepository assetMovementRepository;
    @Mock private SpringDataPortfolioAssetRepository portfolioAssetRepository;
    @Mock private LedgerTransactionRepository ledgerTransactionRepository;
    @Mock private LedgerEntryRepository ledgerEntryRepository;
    @Mock private IdempotencyRecordRepository idempotencyRecordRepository;
    @Mock private KafkaTemplate<String, OrderEvent> kafkaTemplate;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private CreateOrderUseCase useCase;

    private UUID portfolioId;
    private UUID assetId;
    private Portfolio portfolio;
    private Asset asset;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(useCase, "ordersTopic", "custody-orders-topic");
        portfolioId = UUID.randomUUID();
        assetId = UUID.randomUUID();
        portfolio = new Portfolio(portfolioId, UUID.randomUUID());
        asset = new Asset(assetId, "PETR4", "Petrobras", 5);
    }

    @Test
    @DisplayName("SELL with sufficient balance returns PENDING order with idempotencyKey")
    void sellWithSufficientBalance() {
        UUID idempotencyKey = UUID.randomUUID();

        when(idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));

        PortfolioAssetEntity position = new PortfolioAssetEntity();
        position.setId(UUID.randomUUID());
        position.setQuantity(new BigDecimal("100"));
        position.setVersion(1L);
        when(portfolioAssetRepository.findByPortfolioIdAndAssetId(portfolioId, assetId))
                .thenReturn(Optional.of(position));

        UUID orderId = UUID.randomUUID();
        CustodyOrder savedOrder = new CustodyOrder(orderId, portfolio, asset, OrderType.SELL,
                new BigDecimal("50"), OrderStatus.PENDING, idempotencyKey,
                LocalDateTime.now(), LocalDateTime.now());
        when(orderRepository.save(any(CustodyOrder.class))).thenReturn(savedOrder);
        when(ledgerTransactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(assetMovementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(portfolioAssetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(idempotencyRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        try { when(objectMapper.writeValueAsString(any())).thenReturn("{}"); } catch (Exception ignored) {}

        CreateOrderInput input = new CreateOrderInput(portfolioId, assetId, OrderType.SELL, new BigDecimal("50"));
        OrderOutput output = useCase.execute(input, idempotencyKey);

        assertEquals(OrderStatus.PENDING, output.getStatus());
        assertNotNull(output.getIdempotencyKey());
        assertEquals(idempotencyKey, output.getIdempotencyKey());
        assertFalse(output.isAlreadyExisted());
        verify(kafkaTemplate, times(1)).send(eq("custody-orders-topic"), anyString(), any(OrderEvent.class));
    }

    @Test
    @DisplayName("SELL with insufficient balance throws InsufficientBalanceException")
    void sellWithInsufficientBalance() {
        UUID idempotencyKey = UUID.randomUUID();

        when(idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));

        PortfolioAssetEntity position = new PortfolioAssetEntity();
        position.setQuantity(new BigDecimal("100"));
        position.setVersion(1L);
        when(portfolioAssetRepository.findByPortfolioIdAndAssetId(portfolioId, assetId))
                .thenReturn(Optional.of(position));

        CreateOrderInput input = new CreateOrderInput(portfolioId, assetId, OrderType.SELL, new BigDecimal("150"));
        assertThrows(InsufficientBalanceException.class, () -> useCase.execute(input, idempotencyKey));
        verify(orderRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("BUY without existing position creates PortfolioAsset and registers CREDIT")
    void buyWithoutExistingPosition() {
        UUID idempotencyKey = UUID.randomUUID();

        when(idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(portfolioAssetRepository.findByPortfolioIdAndAssetId(portfolioId, assetId))
                .thenReturn(Optional.empty());

        UUID orderId = UUID.randomUUID();
        CustodyOrder savedOrder = new CustodyOrder(orderId, portfolio, asset, OrderType.BUY,
                new BigDecimal("200"), OrderStatus.PENDING, idempotencyKey,
                LocalDateTime.now(), LocalDateTime.now());
        when(orderRepository.save(any(CustodyOrder.class))).thenReturn(savedOrder);
        when(ledgerTransactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(assetMovementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(portfolioAssetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(idempotencyRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        try { when(objectMapper.writeValueAsString(any())).thenReturn("{}"); } catch (Exception ignored) {}

        CreateOrderInput input = new CreateOrderInput(portfolioId, assetId, OrderType.BUY, new BigDecimal("200"));
        OrderOutput output = useCase.execute(input, idempotencyKey);

        assertEquals(OrderStatus.PENDING, output.getStatus());
        assertEquals(OrderType.BUY, output.getType());
        assertFalse(output.isAlreadyExisted());
        verify(portfolioAssetRepository, times(1)).save(any(PortfolioAssetEntity.class));
    }

    @Test
    @DisplayName("Replay with same Idempotency-Key returns existing order without saving")
    void replayWithExistingIdempotencyKey() throws Exception {
        UUID idempotencyKey = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        // Compute the hash the use case will compute
        String data = portfolioId + ":" + assetId + ":" + OrderType.BUY + ":" + new BigDecimal("10");
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String expectedHash = java.util.HexFormat.of().formatHex(hashBytes);

        IdempotencyRecord record = new IdempotencyRecord(UUID.randomUUID(), idempotencyKey,
                expectedHash, "{}", LocalDateTime.now());
        when(idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(record));

        // Create JsonNode for deserialization (use real ObjectMapper to build node)
        com.fasterxml.jackson.databind.ObjectMapper realMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.node.ObjectNode node = realMapper.createObjectNode();
        node.put("id", orderId.toString());
        node.put("portfolioId", portfolioId.toString());
        node.put("assetId", assetId.toString());
        node.put("type", "BUY");
        node.put("quantity", "10");
        node.put("status", "PENDING");
        node.put("idempotencyKey", idempotencyKey.toString());
        node.put("createdAt", LocalDateTime.now().toString());
        node.put("alreadyExisted", false);
        when(objectMapper.readTree(anyString())).thenReturn(node);

        CreateOrderInput input = new CreateOrderInput(portfolioId, assetId, OrderType.BUY, new BigDecimal("10"));
        OrderOutput output = useCase.execute(input, idempotencyKey);

        assertTrue(output.isAlreadyExisted());
        assertEquals(orderId, output.getId());
        verify(orderRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Reuse of Idempotency-Key with different body throws IdempotencyKeyConflictException")
    void reuseKeyWithDifferentBody() {
        UUID idempotencyKey = UUID.randomUUID();

        // Record with hash for quantity=10
        String data10 = portfolioId + ":" + assetId + ":" + OrderType.BUY + ":" + new BigDecimal("10");
        java.security.MessageDigest digest;
        try {
            digest = java.security.MessageDigest.getInstance("SHA-256");
        } catch (Exception e) { throw new RuntimeException(e); }
        byte[] hashBytes = digest.digest(data10.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String hashForQty10 = java.util.HexFormat.of().formatHex(hashBytes);

        IdempotencyRecord record = new IdempotencyRecord(UUID.randomUUID(), idempotencyKey,
                hashForQty10, "{}", LocalDateTime.now());
        when(idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(record));

        // Send with quantity=200 (different hash)
        CreateOrderInput input = new CreateOrderInput(portfolioId, assetId, OrderType.BUY, new BigDecimal("200"));
        assertThrows(IdempotencyKeyConflictException.class, () -> useCase.execute(input, idempotencyKey));
    }

    @Test
    @DisplayName("Null idempotencyKey causes server to generate a non-null UUID")
    void nullIdempotencyKeyServerGenerates() {
        when(idempotencyRecordRepository.findByIdempotencyKey(any(UUID.class))).thenReturn(Optional.empty());
        when(orderRepository.findByIdempotencyKey(any(UUID.class))).thenReturn(Optional.empty());
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(portfolioAssetRepository.findByPortfolioIdAndAssetId(portfolioId, assetId))
                .thenReturn(Optional.empty());

        UUID orderId = UUID.randomUUID();
        CustodyOrder savedOrder = new CustodyOrder(orderId, portfolio, asset, OrderType.BUY,
                new BigDecimal("10"), OrderStatus.PENDING, UUID.randomUUID(),
                LocalDateTime.now(), LocalDateTime.now());
        when(orderRepository.save(any(CustodyOrder.class))).thenReturn(savedOrder);
        when(ledgerTransactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(assetMovementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(portfolioAssetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(idempotencyRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        try { when(objectMapper.writeValueAsString(any())).thenReturn("{}"); } catch (Exception ignored) {}

        CreateOrderInput input = new CreateOrderInput(portfolioId, assetId, OrderType.BUY, new BigDecimal("10"));
        OrderOutput output = useCase.execute(input, null);

        assertNotNull(output.getIdempotencyKey());
    }
}
