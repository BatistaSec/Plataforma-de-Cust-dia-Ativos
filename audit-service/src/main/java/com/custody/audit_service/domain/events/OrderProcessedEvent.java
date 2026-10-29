package com.custody.audit_service.domain.events;

import com.custody.audit_service.domain.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento recebido do event-processor quando uma ordem é processada.
 * Espelho do ProcessedOrderEvent publicado no tópico processed-orders-topic.
 */
public class OrderProcessedEvent {

    private UUID orderId;
    private OrderStatus finalStatus;
    private LocalDateTime processedAt;

    public OrderProcessedEvent() {}

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public OrderStatus getFinalStatus() { return finalStatus; }
    public void setFinalStatus(OrderStatus finalStatus) { this.finalStatus = finalStatus; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
