package com.custody.custody_service.presentation.request;

import com.custody.custody_service.domain.enums.OrderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class CustodyOrderRequest {

    @NotNull(message = "Portfolio ID e obrigatorio")
    private UUID portfolioId;

    @NotNull(message = "Asset ID e obrigatorio")
    private UUID assetId;

    @NotNull(message = "Order type e obrigatorio")
    private OrderType type;

    @NotNull(message = "Quantidade e obrigatorio")
    @DecimalMin(value = "0.000001", message = "quantidade precisa ser maior que zero")
    private BigDecimal quantity;

    public CustodyOrderRequest() {}

    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }

    public UUID getAssetId() { return assetId; }
    public void setAssetId(UUID assetId) { this.assetId = assetId; }

    public OrderType getType() { return type; }
    public void setType(OrderType type) { this.type = type; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}
