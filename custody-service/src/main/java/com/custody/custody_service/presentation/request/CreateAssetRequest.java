package com.custody.custody_service.presentation.request;

import jakarta.validation.constraints.NotBlank;

public class CreateAssetRequest {

    @NotBlank(message = "Ticker é obrigatório")
    private String ticker;

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    public CreateAssetRequest() {}

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
