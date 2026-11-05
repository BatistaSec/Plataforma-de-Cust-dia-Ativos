package com.custody.custody_service.infrastructure.persistence.repositories;

import com.custody.custody_service.infrastructure.persistence.entities.LedgerTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataLedgerTransactionRepository extends JpaRepository<LedgerTransactionEntity, UUID> {
    Optional<LedgerTransactionEntity> findByReference(UUID reference);
}
