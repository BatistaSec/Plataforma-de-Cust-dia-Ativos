package com.custody.custody_service.domain.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Portfolio {

    private UUID id;
    private UUID userId;
    private List<PortfolioAsset> assets = new ArrayList<>();

    public Portfolio(UUID userId) {
        this.userId = userId;
    }

    public Portfolio(UUID id, UUID userId) {
        this.id = id;
        this.userId = userId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }

    public List<PortfolioAsset> getAssets() { return assets; }
    public void setAssets(List<PortfolioAsset> assets) { this.assets = assets; }

    public void addAsset(PortfolioAsset asset) {
        assets.add(asset);
        asset.setPortfolio(this);
    }
}
