package com.custody.custody_service.domain.models;

import com.custody.custody_service.domain.exceptions.InsufficientBalanceException;

import java.math.BigDecimal;
import java.util.UUID;


public class PortfolioAsset {

    private UUID id;
    private Portfolio portfolio;
    private Asset asset;
    private BigDecimal quantity;
    private Long version;

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

    public PortfolioAsset(UUID id, Portfolio portfolio, Asset asset, BigDecimal quantity, Long version) {
        this.id = id;
        this.portfolio = portfolio;
        this.asset = asset;
        this.quantity = quantity;
        this.version = version;
    }

    /**
     * Debits the specified quantity from this position.
     * @throws InsufficientBalanceException if the current quantity is less than the requested amount
     */
    public void debit(BigDecimal quantity) {
        if (this.quantity == null || this.quantity.compareTo(quantity) < 0) {
            throw new InsufficientBalanceException(
                this.quantity != null ? this.quantity : BigDecimal.ZERO,
                quantity
            );
        }
        this.quantity = this.quantity.subtract(quantity);
    }

    /**
     * Credits (adds) the specified quantity to this position.
     */
    public void credit(BigDecimal quantity) {
        if (this.quantity == null) {
            this.quantity = BigDecimal.ZERO;
        }
        this.quantity = this.quantity.add(quantity);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }

    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
