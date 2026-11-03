package com.custody.custody_service.application.usecases;

import com.custody.custody_service.application.dto.CreateOrderInput;
import com.custody.custody_service.application.dto.OrderOutput;
import com.custody.custody_service.domain.enums.OrderStatus;
import com.custody.custody_service.domain.enums.OrderType;
import com.custody.custody_service.domain.events.OrderEvent;
import com.custody.custody_service.domain.exceptions.InsufficientBalanceException;
import com.custody.custody_service.domain.exceptions.ResourceNotFoundException;
import com.custody.custody_service.domain.models.Asset;
import com.custody.custody_service.domain.models.CustodyOrder;
import com.custody.custody_service.domain.models.Portfolio;
import com.custody.custody_service.domain.repositories.AssetMovementRepository;
import com.custody.custody_service.domain.repositories.AssetRepository;
import com.custody.custody_service.domain.repositories.CustodyOrderRepository;
import com.custody.custody_service.domain.repositories.PortfolioRepository;
import com.custody.custody_service.infrastructure.persistence.entities.PortfolioAssetEntity;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataPortfolioAssetRepository;
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
    @Mock private KafkaTemplate<String, OrderEvent> kafkaTemplate;

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
        asset = new Asset(assetId, "PETR4", "Petrobras");
    }

    @Test
    @DisplayName("SELL with sufficient balance returns PENDING order with idempotencyKey")
    void sellWithSufficientBalance() {
        UUID idempotencyKey = UUID.randomUUID();

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
        when(assetMovementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(portfolioAssetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderInput input = new CreateOrderInput(portfolioId, assetId, OrderType.SELL,
                new BigDecimal("50"), idempotencyKey);
        OrderOutput output = useCase.execute(input);

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

        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));

        PortfolioAssetEntity position = new PortfolioAssetEntity();
        position.setQuantity(new BigDecimal("100"));
        position.setVersion(1L);
        when(portfolioAssetRepository.findByPortfolioIdAndAssetId(portfolioId, assetId))
                .thenReturn(Optional.of(position));

        CreateOrderInput input = new CreateOrderInput(portfolioId, assetId, OrderType.SELL,
                new BigDecimal("150"), idempotencyKey);

        assertThrows(InsufficientBalanceException.class, () -> useCase.execute(input));
        verify(orderRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("BUY without existing position creates PortfolioAsset and registers CREDIT")
    void buyWithoutExistingPosition() {
        UUID idempotencyKey = UUID.randomUUID();

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
        when(assetMovementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(portfolioAssetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderInput input = new CreateOrderInput(portfolioId, assetId, OrderType.BUY,
                new BigDecimal("200"), idempotencyKey);
        OrderOutput output = useCase.execute(input);

        assertEquals(OrderStatus.PENDING, output.getStatus());
        assertEquals(OrderType.BUY, output.getType());
        assertFalse(output.isAlreadyExisted());
        verify(portfolioAssetRepository, times(1)).save(any(PortfolioAssetEntity.class));
    }

    @Test
    @DisplayName("Replay with existing idempotencyKey returns existing order without saving again")
    void replayWithExistingIdempotencyKey() {
        UUID idempotencyKey = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        CustodyOrder existingOrder = new CustodyOrder(orderId, portfolio, asset, OrderType.BUY,
                new BigDecimal("10"), OrderStatus.PENDING, idempotencyKey,
                LocalDateTime.now(), LocalDateTime.now());
        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingOrder));

        CreateOrderInput input = new CreateOrderInput(portfolioId, assetId, OrderType.BUY,
                new BigDecimal("10"), idempotencyKey);
        OrderOutput output = useCase.execute(input);

        assertTrue(output.isAlreadyExisted());
        assertEquals(orderId, output.getId());
        verify(orderRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Null idempotencyKey in input causes server to generate a non-null UUID")
    void nullIdempotencyKeyServerGenerates() {
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
        when(assetMovementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(portfolioAssetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderInput input = new CreateOrderInput(portfolioId, assetId, OrderType.BUY,
                new BigDecimal("10"));
        OrderOutput output = useCase.execute(input);

        assertNotNull(output.getIdempotencyKey());
    }

    @Test
    @DisplayName("Portfolio not found throws ResourceNotFoundException")
    void portfolioNotFound() {
        UUID idempotencyKey = UUID.randomUUID();
        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty());

        CreateOrderInput input = new CreateOrderInput(portfolioId, assetId, OrderType.BUY,
                new BigDecimal("10"), idempotencyKey);
        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(input));
    }
}
