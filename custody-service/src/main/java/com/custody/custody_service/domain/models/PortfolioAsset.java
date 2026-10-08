package com.custody.custody_service.domain.models;

import java.math.BigDecimal;
import java.util.UUID;


public class PortfolioAsset {

    private UUID id;
    private Portfolio portfolio;
    private Asset asset;
    private BigDecimal quantity;

    public PortfolioAsset() {}

    public PortfolioAsset(Portfolio portfolio, Asset asset, BigDecimal quantity) {
        this.portfolio = portfolio;
        this.asset = asset;
        this.quantity = quantity;
    }

    public PortfolioAsset(UUID id, Portfolio portfolio, Asset asset, BigDecimal quantity) {
        this.id = id;
        this.portfolio = portfolio;
        this.asset = asset;
        this.quantity = quantity;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }

    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}
