package com.custody.custody_service.infrastructure.persistence.entities;

import com.custody.custody_service.domain.enums.MovementType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for the asset_movements table — an immutable audit record.
 * All data fields are marked @Column(updatable = false) so Hibernate
 * will never issue an UPDATE for them after the initial INSERT.
 */
@Entity
@Table(name = "asset_movements", indexes = {
    @Index(name = "idx_asset_mov_portfolio_asset_time",
           columnList = "portfolio_id, asset_id, occurred_at")
})
public class AssetMovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "portfolio_id", updatable = false, nullable = false)
    private UUID portfolioId;

    @Column(name = "asset_id", updatable = false, nullable = false)
    private UUID assetId;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false, nullable = false)
    private MovementType type;

    @Column(updatable = false, nullable = false, precision = 38, scale = 5)
    private BigDecimal quantity;

    @Column(name = "reference_order_id", updatable = false, nullable = false)
    private UUID referenceOrderId;

    @Column(updatable = false, nullable = false)
    private LocalDateTime occurredAt;

    @PrePersist
    protected void onCreate() {
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
    }

    public AssetMovementEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }

    public UUID getAssetId() { return assetId; }
    public void setAssetId(UUID assetId) { this.assetId = assetId; }

    public MovementType getType() { return type; }
    public void setType(MovementType type) { this.type = type; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public UUID getReferenceOrderId() { return referenceOrderId; }
    public void setReferenceOrderId(UUID referenceOrderId) { this.referenceOrderId = referenceOrderId; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
