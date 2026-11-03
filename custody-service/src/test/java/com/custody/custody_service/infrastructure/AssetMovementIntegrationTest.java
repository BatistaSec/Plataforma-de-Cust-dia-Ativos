package com.custody.custody_service.infrastructure;

import com.custody.custody_service.domain.enums.MovementType;
import com.custody.custody_service.infrastructure.persistence.entities.AssetMovementEntity;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataAssetMovementRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("AssetMovement — immutability and index integration tests")
class AssetMovementIntegrationTest {

    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Container @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Autowired private SpringDataAssetMovementRepository assetMovementRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        assetMovementRepository.deleteAll();
    }

    @Test
    @DisplayName("UPDATABLE=false columns cannot be modified after insert — quantity stays unchanged")
    void updatableFalsePreventsModification() {
        UUID portfolioId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID referenceOrderId = UUID.randomUUID();
        BigDecimal originalQty = new BigDecimal("50");

        // Insert
        AssetMovementEntity entity = new AssetMovementEntity();
        entity.setPortfolioId(portfolioId);
        entity.setAssetId(assetId);
        entity.setType(MovementType.CREDIT);
        entity.setQuantity(originalQty);
        entity.setReferenceOrderId(referenceOrderId);
        entity.setOccurredAt(LocalDateTime.now());
        entity = assetMovementRepository.save(entity);

        entityManager.flush();
        entityManager.clear();

        // Load and attempt to modify
        var loaded = assetMovementRepository.findById(entity.getId()).orElseThrow();
        assertEquals(originalQty, loaded.getQuantity());

        loaded.setQuantity(new BigDecimal("999"));
        loaded.setType(MovementType.DEBIT);
        assetMovementRepository.save(loaded);

        entityManager.flush();
        entityManager.clear();

        // Reload — values should be unchanged because @Column(updatable = false)
        var reloaded = assetMovementRepository.findById(entity.getId()).orElseThrow();
        assertEquals(originalQty, reloaded.getQuantity(),
                "quantity should not change — column is updatable=false");
        assertEquals(MovementType.CREDIT, reloaded.getType(),
                "type should not change — column is updatable=false");
    }

    @Test
    @DisplayName("Index on (portfolio_id, asset_id, occurred_at) exists in database")
    void indexExists() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'idx_asset_mov_portfolio_asset_time'",
                Integer.class
        );
        assertNotNull(count);
        assertTrue(count > 0, "Index idx_asset_mov_portfolio_asset_time should exist in the database");
    }

    @Test
    @DisplayName("referenceOrderId is always populated and points to the generating order")
    void referenceOrderIdAlwaysPopulated() {
        UUID orderId = UUID.randomUUID();

        AssetMovementEntity entity = new AssetMovementEntity();
        entity.setPortfolioId(UUID.randomUUID());
        entity.setAssetId(UUID.randomUUID());
        entity.setType(MovementType.DEBIT);
        entity.setQuantity(new BigDecimal("30"));
        entity.setReferenceOrderId(orderId);
        entity.setOccurredAt(LocalDateTime.now());
        entity = assetMovementRepository.save(entity);

        entityManager.flush();
        entityManager.clear();

        var loaded = assetMovementRepository.findById(entity.getId()).orElseThrow();
        assertEquals(orderId, loaded.getReferenceOrderId());
    }
}
