package com.custody.custody_service.infrastructure.persistence.repositories;

import com.custody.custody_service.infrastructure.persistence.entities.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPortfolioRepository extends JpaRepository<PortfolioEntity, UUID> {
    Optional<PortfolioEntity> findByUserId(UUID userId);
}
