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

    public OrderOutput(UUID id, UUID portfolioId, UUID assetId, OrderType type,
                       BigDecimal quantity, OrderStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.assetId = assetId;
        this.type = type;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getPortfolioId() { return portfolioId; }
    public UUID getAssetId() { return assetId; }
    public OrderType getType() { return type; }
    public BigDecimal getQuantity() { return quantity; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
