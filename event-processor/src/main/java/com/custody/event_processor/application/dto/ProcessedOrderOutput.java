package com.custody.event_processor.application.dto;

import com.custody.event_processor.domain.enums.OrderStatus;
import com.custody.event_processor.domain.enums.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProcessedOrderOutput {
    private UUID id;
    private UUID orderId;
    private UUID portfolioId;
    private UUID assetId;
    private OrderType type;
    private BigDecimal quantity;
    private OrderStatus status;
    private LocalDateTime processedAt;

    public ProcessedOrderOutput(UUID id, UUID orderId, UUID portfolioId, UUID assetId, OrderType type, BigDecimal quantity, OrderStatus status, LocalDateTime processedAt) {
        this.id = id;
        this.orderId = orderId;
        this.portfolioId = portfolioId;
        this.assetId = assetId;
        this.type = type;
        this.quantity = quantity;
        this.status = status;
        this.processedAt = processedAt;
    }


    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getPortfolioId() { return portfolioId; }
    public UUID getAssetId() { return assetId; }
    public OrderType getType() { return type; }
    public BigDecimal getQuantity() { return quantity; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getProcessedAt() { return processedAt; }
}
