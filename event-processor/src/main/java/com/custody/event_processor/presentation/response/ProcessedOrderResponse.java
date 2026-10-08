package com.custody.event_processor.presentation.response;

import com.custody.event_processor.domain.enums.OrderStatus;
import com.custody.event_processor.domain.enums.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProcessedOrderResponse {
    private UUID id;
    private UUID orderId;
    private OrderType type;
    private BigDecimal quantity;
    private OrderStatus status;
    private LocalDateTime processedAt;

    public ProcessedOrderResponse(UUID id, UUID orderId, OrderType type, BigDecimal quantity, OrderStatus status, LocalDateTime processedAt) {
        this.id = id;
        this.orderId = orderId;
        this.type = type;
        this.quantity = quantity;
        this.status = status;
        this.processedAt = processedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public OrderType getType() { return type; }
    public BigDecimal getQuantity() { return quantity; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getProcessedAt() { return processedAt; }
}
