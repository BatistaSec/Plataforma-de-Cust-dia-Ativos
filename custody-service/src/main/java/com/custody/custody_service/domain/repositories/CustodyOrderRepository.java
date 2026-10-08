package com.custody.custody_service.domain.repositories;

import com.custody.custody_service.domain.models.CustodyOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface CustodyOrderRepository {

    CustodyOrder save(CustodyOrder order);

    Optional<CustodyOrder> findById(UUID id);

    List<CustodyOrder> findByPortfolioId(UUID portfolioId);
}
