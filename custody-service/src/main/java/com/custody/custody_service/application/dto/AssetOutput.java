package com.custody.custody_service.application.dto;

import java.util.UUID;

public class AssetOutput {

    private final UUID id;
    private final String ticker;
    private final String name;

    public AssetOutput(UUID id, String ticker, String name) {
        this.id = id;
        this.ticker = ticker;
        this.name = name;
    }

    public UUID getId() { return id; }
    public String getTicker() { return ticker; }
    public String getName() { return name; }
}
