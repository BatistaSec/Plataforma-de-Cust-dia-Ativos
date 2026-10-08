package com.custody.custody_service.domain.repositories;

import com.custody.custody_service.domain.models.Portfolio;

import java.util.Optional;
import java.util.UUID;


public interface PortfolioRepository {

    Portfolio save(Portfolio portfolio);

    Optional<Portfolio> findById(UUID id);

    Optional<Portfolio> findByUserId(UUID userId);
}
