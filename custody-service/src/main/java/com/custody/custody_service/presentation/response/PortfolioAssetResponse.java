package com.custody.custody_service.presentation.response;

import java.math.BigDecimal;
import java.util.UUID;

public class PortfolioAssetResponse {

    private UUID id;
    private UUID assetId;
    private String ticker;
    private String assetName;
    private BigDecimal quantity;

    public PortfolioAssetResponse() {}

    public PortfolioAssetResponse(UUID id, UUID assetId, String ticker, String assetName, BigDecimal quantity) {
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
