package com.custody.custody_service.application.usecases;

import com.custody.custody_service.application.dto.PortfolioAssetOutput;
import com.custody.custody_service.application.dto.PortfolioOutput;
import com.custody.custody_service.domain.exceptions.ResourceNotFoundException;
import com.custody.custody_service.domain.models.Portfolio;
import com.custody.custody_service.domain.repositories.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetPortfolioUseCase {

    private final PortfolioRepository portfolioRepository;

    public GetPortfolioUseCase(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    @Transactional(readOnly = true)
    public PortfolioOutput execute(UUID userId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio não encontrado para o usuário: " + userId));

        var assetOutputs = portfolio.getAssets().stream()
                .map(pa -> new PortfolioAssetOutput(
                        pa.getId(),
                        pa.getAsset().getId(),
                        pa.getAsset().getTicker(),
                        pa.getAsset().getName(),
                        pa.getQuantity()
                ))
                .collect(Collectors.toList());

        return new PortfolioOutput(portfolio.getId(), portfolio.getUserId(), assetOutputs);
    }
}
