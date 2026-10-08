package com.custody.custody_service.domain.models;

import com.custody.custody_service.domain.enums.OrderStatus;
import com.custody.custody_service.domain.enums.OrderType;
import com.custody.custody_service.domain.exceptions.InvalidQuantityException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


public class CustodyOrder {

    private UUID id;
    private Portfolio portfolio;
    private Asset asset;
    private OrderType type;
    private BigDecimal quantity;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CustodyOrder() {}

    public CustodyOrder(Portfolio portfolio, Asset asset, OrderType type, BigDecimal quantity, OrderStatus status) {
        validateQuantity(quantity);
        this.portfolio = portfolio;
        this.asset = asset;
        this.type = type;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public CustodyOrder(UUID id, Portfolio portfolio, Asset asset, OrderType type,
                         BigDecimal quantity, OrderStatus status,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.portfolio = portfolio;
        this.asset = asset;
        this.type = type;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuantityException("A quantidade deve ser maior que zero");
        }
    }

    // Getters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Portfolio getPortfolio() { return portfolio; }
    public Asset getAsset() { return asset; }
    public OrderType getType() { return type; }
    public BigDecimal getQuantity() { return quantity; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}
