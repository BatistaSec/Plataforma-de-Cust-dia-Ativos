package com.custody.custody_service.application.dto;

import com.custody.custody_service.domain.enums.OrderType;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateOrderInput {

    private final UUID portfolioId;
    private final UUID assetId;
    private final OrderType type;
    private final BigDecimal quantity;
    private final UUID idempotencyKey;

    public CreateOrderInput(UUID portfolioId, UUID assetId, OrderType type, BigDecimal quantity) {
        this(portfolioId, assetId, type, quantity, null);
    }

    public CreateOrderInput(UUID portfolioId, UUID assetId, OrderType type, BigDecimal quantity, UUID idempotencyKey) {
        this.portfolioId = portfolioId;
        this.assetId = assetId;
        this.type = type;
        this.quantity = quantity;
        this.idempotencyKey = idempotencyKey;
    }

    public UUID getPortfolioId() { return portfolioId; }
    public UUID getAssetId() { return assetId; }
    public OrderType getType() { return type; }
    public BigDecimal getQuantity() { return quantity; }
    public UUID getIdempotencyKey() { return idempotencyKey; }
}
