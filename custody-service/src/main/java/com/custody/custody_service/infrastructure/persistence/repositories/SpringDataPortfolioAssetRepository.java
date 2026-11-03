package com.custody.custody_service.infrastructure.persistence.repositories;

import com.custody.custody_service.infrastructure.persistence.entities.PortfolioAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPortfolioAssetRepository extends JpaRepository<PortfolioAssetEntity, UUID> {
    Optional<PortfolioAssetEntity> findByPortfolioIdAndAssetId(UUID portfolioId, UUID assetId);
}
