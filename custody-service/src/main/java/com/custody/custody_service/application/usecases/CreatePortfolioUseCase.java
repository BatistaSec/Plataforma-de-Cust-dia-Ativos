package com.custody.custody_service.application.usecases;

import com.custody.custody_service.application.dto.CreatePortfolioInput;
import com.custody.custody_service.application.dto.PortfolioAssetOutput;
import com.custody.custody_service.application.dto.PortfolioOutput;
import com.custody.custody_service.domain.exceptions.PortfolioAlreadyExistsException;
import com.custody.custody_service.domain.models.Portfolio;
import com.custody.custody_service.domain.repositories.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class CreatePortfolioUseCase {

    private final PortfolioRepository portfolioRepository;

    public CreatePortfolioUseCase(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    @Transactional
    public PortfolioOutput execute(CreatePortfolioInput input) {
        if (portfolioRepository.findByUserId(input.getUserId()).isPresent()) {
            throw new PortfolioAlreadyExistsException("Portfolio já existe para este usuário");
        }

        Portfolio portfolio = new Portfolio(input.getUserId());
        Portfolio saved = portfolioRepository.save(portfolio);

        return new PortfolioOutput(saved.getId(), saved.getUserId(), Collections.emptyList());
    }
}
