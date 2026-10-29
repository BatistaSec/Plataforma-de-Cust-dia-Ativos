package com.custody.custody_service.presentation.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class GetPortfolioRequest {

    @NotNull(message = "User ID é obrigatório")
    private UUID userId;

    public GetPortfolioRequest() {}

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}
