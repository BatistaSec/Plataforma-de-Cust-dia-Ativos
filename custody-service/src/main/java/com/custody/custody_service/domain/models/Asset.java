package com.custody.custody_service.domain.models;

import java.util.UUID;


public class Asset {

    private UUID id;
    private String ticker;
    private String name;

    public Asset(String ticker, String name) {
        this.ticker = ticker;
        this.name = name;
    }

    public Asset(UUID id, String ticker, String name) {
        this.id = id;
        this.ticker = ticker;
        this.name = name;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTicker() { return ticker; }
    public String getName() { return name; }
}
