package com.custody.audit_service.domain.events;

import com.custody.audit_service.domain.enums.OrderStatus;
import com.custody.audit_service.domain.enums.OrderType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento recebido do custody-service quando uma ordem é criada.
 * Espelho do OrderEvent publicado no tópico custody-orders-topic.
 */
public class OrderCreatedEvent {

    private UUID orderId;
    private UUID portfolioId;
    private UUID assetId;
    private OrderType type;
    private BigDecimal quantity;
    private OrderStatus status;

    public OrderCreatedEvent() {}

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
}
