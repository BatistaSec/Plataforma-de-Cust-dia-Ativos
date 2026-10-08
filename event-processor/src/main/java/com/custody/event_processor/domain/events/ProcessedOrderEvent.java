package com.custody.event_processor.domain.events;

import com.custody.event_processor.domain.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProcessedOrderEvent {
    private UUID orderId;
    private OrderStatus finalStatus;
    private LocalDateTime processedAt;

    public ProcessedOrderEvent() {}

    public ProcessedOrderEvent(UUID orderId, OrderStatus finalStatus, LocalDateTime processedAt) {
        this.orderId = orderId;
        this.finalStatus = finalStatus;
        this.processedAt = processedAt;
    }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public OrderStatus getFinalStatus() { return finalStatus; }
    public void setFinalStatus(OrderStatus finalStatus) { this.finalStatus = finalStatus; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
