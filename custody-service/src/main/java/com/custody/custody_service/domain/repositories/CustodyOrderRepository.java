package com.custody.custody_service.domain.repositories;

import com.custody.custody_service.domain.models.CustodyOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustodyOrderRepository extends JpaRepository<CustodyOrder, UUID> {
    List<CustodyOrder> findByPortfolioId(UUID portfolioId);
}
