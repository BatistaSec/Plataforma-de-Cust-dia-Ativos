package com.custody.custody_service.application.dto;

import com.custody.custody_service.domain.enums.OrderStatus;
import com.custody.custody_service.domain.enums.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class OrderOutput {

    private final UUID id;
    private final UUID portfolioId;
    private final UUID assetId;
    private final OrderType type;
    private final BigDecimal quantity;
    private final OrderStatus status;
    private final LocalDateTime createdAt;
    private final UUID idempotencyKey;
    private final boolean alreadyExisted;

    public OrderOutput(UUID id, UUID portfolioId, UUID assetId, OrderType type,
                       BigDecimal quantity, OrderStatus status, LocalDateTime createdAt) {
        this(id, portfolioId, assetId, type, quantity, status, createdAt, null, false);
    }

    public OrderOutput(UUID id, UUID portfolioId, UUID assetId, OrderType type,
                       BigDecimal quantity, OrderStatus status, LocalDateTime createdAt,
                       UUID idempotencyKey, boolean alreadyExisted) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.assetId = assetId;
        this.type = type;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
        this.idempotencyKey = idempotencyKey;
        this.alreadyExisted = alreadyExisted;
    }

    public UUID getId() { return id; }
    public UUID getPortfolioId() { return portfolioId; }
    public UUID getAssetId() { return assetId; }
    public OrderType getType() { return type; }
    public BigDecimal getQuantity() { return quantity; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public UUID getIdempotencyKey() { return idempotencyKey; }
    public boolean isAlreadyExisted() { return alreadyExisted; }
}
