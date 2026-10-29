package com.custody.custody_service.presentation.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreatePortfolioRequest {

    @NotNull(message = "User ID é obrigatório")
    private UUID userId;

    public CreatePortfolioRequest() {}

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}
