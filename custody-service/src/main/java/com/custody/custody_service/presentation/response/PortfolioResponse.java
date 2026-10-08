package com.custody.custody_service.presentation.response;

import java.util.List;
import java.util.UUID;

public class PortfolioResponse {

    private UUID id;
    private UUID userId;
    private List<PortfolioAssetResponse> assets;

    public PortfolioResponse() {}

    public PortfolioResponse(UUID id, UUID userId, List<PortfolioAssetResponse> assets) {
        this.id = id;
        this.userId = userId;
        this.assets = assets;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public List<PortfolioAssetResponse> getAssets() { return assets; }
}
