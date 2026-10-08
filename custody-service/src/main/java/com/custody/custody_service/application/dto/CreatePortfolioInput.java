package com.custody.custody_service.application.dto;

import java.util.UUID;

public class CreatePortfolioInput {

    private final UUID userId;

    public CreatePortfolioInput(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() { return userId; }
}
