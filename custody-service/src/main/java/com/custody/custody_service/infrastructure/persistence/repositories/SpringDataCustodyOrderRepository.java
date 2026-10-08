package com.custody.custody_service.infrastructure.persistence.repositories;

import com.custody.custody_service.infrastructure.persistence.entities.CustodyOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataCustodyOrderRepository extends JpaRepository<CustodyOrderEntity, UUID> {
    List<CustodyOrderEntity> findByPortfolioId(UUID portfolioId);
}
