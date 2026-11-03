package com.custody.custody_service.domain.models;

import com.custody.custody_service.domain.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable domain model representing a single debit or credit of an asset position.
 * Once constructed, no field can be changed — there are no public setters.
 */
public class AssetMovement {

    private final UUID id;
    private final UUID portfolioId;
    private final UUID assetId;
    private final MovementType type;
    private final BigDecimal quantity;
    private final UUID referenceOrderId;
    private final LocalDateTime occurredAt;

    public AssetMovement(UUID id, UUID portfolioId, UUID assetId, MovementType type,
                         BigDecimal quantity, UUID referenceOrderId, LocalDateTime occurredAt) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.assetId = assetId;
        this.type = type;
        this.quantity = quantity;
        this.referenceOrderId = referenceOrderId;
        this.occurredAt = occurredAt;
    }

    public UUID getId() { return id; }
    public UUID getPortfolioId() { return portfolioId; }
    public UUID getAssetId() { return assetId; }
    public MovementType getType() { return type; }
    public BigDecimal getQuantity() { return quantity; }
    public UUID getReferenceOrderId() { return referenceOrderId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
