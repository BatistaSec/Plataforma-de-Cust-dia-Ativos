package com.custody.custody_service.presentation.response;

import java.util.UUID;

public class AssetResponse {

    private UUID id;
    private String ticker;
    private String name;

    public AssetResponse() {}

    public AssetResponse(UUID id, String ticker, String name) {
        this.id = id;
        this.ticker = ticker;
        this.name = name;
    }

    public UUID getId() { return id; }
    public String getTicker() { return ticker; }
    public String getName() { return name; }
}
