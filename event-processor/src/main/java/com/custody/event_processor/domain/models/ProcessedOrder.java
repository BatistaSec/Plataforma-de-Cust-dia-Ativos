package com.custody.event_processor.domain.models;

import com.custody.event_processor.domain.enums.OrderStatus;
import com.custody.event_processor.domain.enums.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProcessedOrder {
    private UUID id;
    private UUID orderId;
    private UUID portfolioId;
    private UUID assetId;
    private OrderType type;
    private BigDecimal quantity;
    private OrderStatus status;
    private LocalDateTime processedAt;

    public ProcessedOrder() {}

    public ProcessedOrder(UUID id, UUID orderId, UUID portfolioId, UUID assetId, OrderType type, BigDecimal quantity, OrderStatus status, LocalDateTime processedAt) {
        this.id = id;
        this.orderId = orderId;
        this.portfolioId = portfolioId;
        this.assetId = assetId;
        this.type = type;
        this.quantity = quantity;
        this.status = status;
        this.processedAt = processedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }

    public UUID getAssetId() { return assetId; }
    public void setAssetId(UUID assetId) { this.assetId = assetId; }

    public OrderType getType() { return type; }
    public void setType(OrderType type) { this.type = type; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
