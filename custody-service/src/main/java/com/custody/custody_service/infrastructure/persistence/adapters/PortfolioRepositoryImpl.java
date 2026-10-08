package com.custody.custody_service.infrastructure.persistence.adapters;

import com.custody.custody_service.domain.models.Portfolio;
import com.custody.custody_service.domain.repositories.PortfolioRepository;
import com.custody.custody_service.infrastructure.mappers.PortfolioMapper;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataPortfolioRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PortfolioRepositoryImpl implements PortfolioRepository {

    private final SpringDataPortfolioRepository springDataRepository;

    public PortfolioRepositoryImpl(SpringDataPortfolioRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Portfolio save(Portfolio portfolio) {
        var entity = PortfolioMapper.toEntity(portfolio);
        var saved = springDataRepository.save(entity);
        return PortfolioMapper.toDomain(saved);
    }

    @Override
    public Optional<Portfolio> findById(UUID id) {
        return springDataRepository.findById(id).map(PortfolioMapper::toDomain);
    }

    @Override
    public Optional<Portfolio> findByUserId(UUID userId) {
        return springDataRepository.findByUserId(userId).map(PortfolioMapper::toDomain);
    }
}
