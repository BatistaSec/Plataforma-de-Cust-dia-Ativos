package com.custody.custody_service.application.dto;

import java.util.List;
import java.util.UUID;

public class PortfolioOutput {

    private final UUID id;
    private final UUID userId;
    private final List<PortfolioAssetOutput> assets;

    public PortfolioOutput(UUID id, UUID userId, List<PortfolioAssetOutput> assets) {
        this.id = id;
        this.userId = userId;
        this.assets = assets;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public List<PortfolioAssetOutput> getAssets() { return assets; }
}
