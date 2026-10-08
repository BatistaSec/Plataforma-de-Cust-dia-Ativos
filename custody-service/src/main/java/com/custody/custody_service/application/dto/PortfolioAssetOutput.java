package com.custody.custody_service.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PortfolioAssetOutput {

    private final UUID id;
    private final UUID assetId;
    private final String ticker;
    private final String assetName;
    private final BigDecimal quantity;

    public PortfolioAssetOutput(UUID id, UUID assetId, String ticker, String assetName, BigDecimal quantity) {
        this.id = id;
        this.assetId = assetId;
        this.ticker = ticker;
        this.assetName = assetName;
        this.quantity = quantity;
    }

    public UUID getId() { return id; }
    public UUID getAssetId() { return assetId; }
    public String getTicker() { return ticker; }
    public String getAssetName() { return assetName; }
    public BigDecimal getQuantity() { return quantity; }
}
