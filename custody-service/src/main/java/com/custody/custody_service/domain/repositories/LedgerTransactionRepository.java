package com.custody.custody_service.domain.repositories;

import com.custody.custody_service.domain.models.LedgerTransaction;

import java.util.Optional;
import java.util.UUID;

public interface LedgerTransactionRepository {

    LedgerTransaction save(LedgerTransaction transaction);

    Optional<LedgerTransaction> findByReference(UUID reference);
}
