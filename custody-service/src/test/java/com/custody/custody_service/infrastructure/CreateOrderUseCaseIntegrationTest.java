package com.custody.custody_service.infrastructure;

import com.custody.custody_service.infrastructure.persistence.entities.AssetEntity;
import com.custody.custody_service.infrastructure.persistence.entities.PortfolioAssetEntity;
import com.custody.custody_service.infrastructure.persistence.entities.PortfolioEntity;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataAssetMovementRepository;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataCustodyOrderRepository;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataPortfolioAssetRepository;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataPortfolioRepository;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataAssetRepository;
import com.custody.custody_service.presentation.response.CustodyOrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("CreateOrderUseCase — integration tests with Testcontainers")
class CreateOrderUseCaseIntegrationTest {

    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Container @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private SpringDataPortfolioRepository portfolioRepository;
    @Autowired private SpringDataAssetRepository assetRepository;
    @Autowired private SpringDataPortfolioAssetRepository portfolioAssetRepository;
    @Autowired private SpringDataCustodyOrderRepository custodyOrderRepository;
    @Autowired private SpringDataAssetMovementRepository assetMovementRepository;

    @BeforeEach
    void setUp() {
        assetMovementRepository.deleteAll();
        custodyOrderRepository.deleteAll();
        portfolioAssetRepository.deleteAll();
        portfolioRepository.deleteAll();
        assetRepository.deleteAll();
    }

    private PortfolioEntity createPortfolio() {
        PortfolioEntity portfolio = new PortfolioEntity();
        portfolio.setUserId(UUID.randomUUID());
        return portfolioRepository.save(portfolio);
    }

    private AssetEntity createAsset(String ticker) {
        AssetEntity asset = new AssetEntity();
        asset.setTicker(ticker);
        asset.setName(ticker + " Inc");
        return assetRepository.save(asset);
    }

    private void createPosition(PortfolioEntity portfolio, AssetEntity asset, String quantity) {
        PortfolioAssetEntity position = new PortfolioAssetEntity();
        position.setPortfolio(portfolio);
        position.setAsset(asset);
        position.setQuantity(new BigDecimal(quantity));
        portfolioAssetRepository.save(position);
    }

    @Test
    @DisplayName("SELL with sufficient balance creates order, DEBIT movement, and updates position")
    void sellWithSufficientBalance() {
        PortfolioEntity portfolio = createPortfolio();
        AssetEntity asset = createAsset("PETR4");
        createPosition(portfolio, asset, "100");

        Map<String, Object> request = Map.of(
                "portfolioId", portfolio.getId(),
                "assetId", asset.getId(),
                "type", "SELL",
                "quantity", new BigDecimal("50"),
                "idempotencyKey", UUID.randomUUID()
        );

        ResponseEntity<CustodyOrderResponse> response = restTemplate.postForEntity(
                "/api/v1/custody/orders", request, CustodyOrderResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        // Verify position was debited
        var position = portfolioAssetRepository.findByPortfolioIdAndAssetId(
                portfolio.getId(), asset.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("50").compareTo(position.getQuantity()));

        // Verify DEBIT movement was created
        var movements = assetMovementRepository.findAll();
        assertEquals(1, movements.size());
        assertEquals("DEBIT", movements.get(0).getType().name());
        assertEquals(0, new BigDecimal("50").compareTo(movements.get(0).getQuantity()));
    }

    @Test
    @DisplayName("SELL with insufficient balance returns 422 and creates no records")
    void sellWithInsufficientBalance() {
        PortfolioEntity portfolio = createPortfolio();
        AssetEntity asset = createAsset("PETR4");
        createPosition(portfolio, asset, "100");

        Map<String, Object> request = Map.of(
                "portfolioId", portfolio.getId(),
                "assetId", asset.getId(),
                "type", "SELL",
                "quantity", new BigDecimal("150"),
                "idempotencyKey", UUID.randomUUID()
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/custody/orders", request, String.class);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());

        // No order or movement created
        assertEquals(0, custodyOrderRepository.count());
        assertEquals(0, assetMovementRepository.count());

        // Position unchanged
        var position = portfolioAssetRepository.findByPortfolioIdAndAssetId(
                portfolio.getId(), asset.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("100").compareTo(position.getQuantity()));
    }

    @Test
    @DisplayName("Two concurrent SELL of 60 on position of 100: one 201, one 409, final = 40")
    void concurrentSellConflict() throws Exception {
        PortfolioEntity portfolio = createPortfolio();
        AssetEntity asset = createAsset("PETR4");
        createPosition(portfolio, asset, "100");

        Map<String, Object> request1 = Map.of(
                "portfolioId", portfolio.getId(),
                "assetId", asset.getId(),
                "type", "SELL",
                "quantity", new BigDecimal("60"),
                "idempotencyKey", UUID.randomUUID()
        );
        Map<String, Object> request2 = Map.of(
                "portfolioId", portfolio.getId(),
                "assetId", asset.getId(),
                "type", "SELL",
                "quantity", new BigDecimal("60"),
                "idempotencyKey", UUID.randomUUID()
        );

        AtomicReference<ResponseEntity<String>> resp1 = new AtomicReference<>();
        AtomicReference<ResponseEntity<String>> resp2 = new AtomicReference<>();

        CompletableFuture<Void> f1 = CompletableFuture.runAsync(() ->
                resp1.set(restTemplate.postForEntity("/api/v1/custody/orders", request1, String.class)));
        CompletableFuture<Void> f2 = CompletableFuture.runAsync(() ->
                resp2.set(restTemplate.postForEntity("/api/v1/custody/orders", request2, String.class)));

        CompletableFuture.allOf(f1, f2).join();

        org.springframework.http.HttpStatusCode s1 = resp1.get().getStatusCode();
        org.springframework.http.HttpStatusCode s2 = resp2.get().getStatusCode();

        // One succeeds (201), the other fails (409 optimistic lock or 422 insufficient balance if serialized)
        boolean oneSucceeded = s1.equals(HttpStatus.CREATED) || s2.equals(HttpStatus.CREATED);
        boolean bothSucceeded = s1.equals(HttpStatus.CREATED) && s2.equals(HttpStatus.CREATED);
        assertTrue(oneSucceeded && !bothSucceeded,
                "Expected exactly one 201, got " + s1 + " and " + s2);

        // Final position should be 40 (100 - 60), never negative
        var position = portfolioAssetRepository.findByPortfolioIdAndAssetId(
                portfolio.getId(), asset.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("40").compareTo(position.getQuantity()));
    }

    @Test
    @DisplayName("Replay with same idempotencyKey returns 200 OK with same order")
    void replayWithSameIdempotencyKey() {
        PortfolioEntity portfolio = createPortfolio();
        AssetEntity asset = createAsset("PETR4");
        UUID idempotencyKey = UUID.randomUUID();

        Map<String, Object> request = Map.of(
                "portfolioId", portfolio.getId(),
                "assetId", asset.getId(),
                "type", "BUY",
                "quantity", new BigDecimal("10"),
                "idempotencyKey", idempotencyKey
        );

        ResponseEntity<CustodyOrderResponse> response1 = restTemplate.postForEntity(
                "/api/v1/custody/orders", request, CustodyOrderResponse.class);
        assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        UUID orderId = response1.getBody().getId();

        ResponseEntity<CustodyOrderResponse> response2 = restTemplate.postForEntity(
                "/api/v1/custody/orders", request, CustodyOrderResponse.class);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(orderId, response2.getBody().getId());

        // Only one order in DB
        assertEquals(1, custodyOrderRepository.count());
    }

    @Test
    @DisplayName("BUY creates position from zero and registers CREDIT movement")
    void buyCreatesPositionFromZero() {
        PortfolioEntity portfolio = createPortfolio();
        AssetEntity asset = createAsset("VALE3");

        Map<String, Object> request = Map.of(
                "portfolioId", portfolio.getId(),
                "assetId", asset.getId(),
                "type", "BUY",
                "quantity", new BigDecimal("200"),
                "idempotencyKey", UUID.randomUUID()
        );

        ResponseEntity<CustodyOrderResponse> response = restTemplate.postForEntity(
                "/api/v1/custody/orders", request, CustodyOrderResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        // Position created
        var position = portfolioAssetRepository.findByPortfolioIdAndAssetId(
                portfolio.getId(), asset.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("200").compareTo(position.getQuantity()));

        // CREDIT movement created
        var movements = assetMovementRepository.findAll();
        assertEquals(1, movements.size());
        assertEquals("CREDIT", movements.get(0).getType().name());
    }

    @Test
    @DisplayName("Five decimal places preserved end-to-end")
    void fiveDecimalPrecisionPreserved() {
        PortfolioEntity portfolio = createPortfolio();
        AssetEntity asset = createAsset("PREC5");

        BigDecimal qty = new BigDecimal("0.00001");

        Map<String, Object> request = Map.of(
                "portfolioId", portfolio.getId(),
                "assetId", asset.getId(),
                "type", "BUY",
                "quantity", qty,
                "idempotencyKey", UUID.randomUUID()
        );

        ResponseEntity<CustodyOrderResponse> response = restTemplate.postForEntity(
                "/api/v1/custody/orders", request, CustodyOrderResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(0, qty.compareTo(response.getBody().getQuantity()));

        // Check position
        var position = portfolioAssetRepository.findByPortfolioIdAndAssetId(
                portfolio.getId(), asset.getId()).orElseThrow();
        assertEquals(0, qty.compareTo(position.getQuantity()));

        // Check movement
        var movements = assetMovementRepository.findAll();
        assertEquals(1, movements.size());
        assertEquals(0, qty.compareTo(movements.get(0).getQuantity()));
    }
}
