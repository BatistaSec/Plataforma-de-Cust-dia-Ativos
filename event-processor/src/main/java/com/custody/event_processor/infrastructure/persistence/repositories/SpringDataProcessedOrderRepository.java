package com.custody.event_processor.infrastructure.persistence.repositories;

import com.custody.event_processor.infrastructure.persistence.entities.ProcessedOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataProcessedOrderRepository extends JpaRepository<ProcessedOrderEntity, UUID> {
    Optional<ProcessedOrderEntity> findByOrderId(UUID orderId);
}
