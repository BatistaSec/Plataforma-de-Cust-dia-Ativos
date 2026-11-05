package com.custody.custody_service.infrastructure.persistence.adapters;

import com.custody.custody_service.domain.models.LedgerTransaction;
import com.custody.custody_service.domain.repositories.LedgerTransactionRepository;
import com.custody.custody_service.infrastructure.mappers.LedgerTransactionMapper;
import com.custody.custody_service.infrastructure.persistence.repositories.SpringDataLedgerTransactionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LedgerTransactionRepositoryImpl implements LedgerTransactionRepository {

    private final SpringDataLedgerTransactionRepository springDataRepository;

    public LedgerTransactionRepositoryImpl(SpringDataLedgerTransactionRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public LedgerTransaction save(LedgerTransaction transaction) {
        var entity = LedgerTransactionMapper.toEntity(transaction);
        var saved = springDataRepository.save(entity);
        return LedgerTransactionMapper.toDomain(saved);
    }

    @Override
    public Optional<LedgerTransaction> findByReference(UUID reference) {
        return springDataRepository.findByReference(reference).map(LedgerTransactionMapper::toDomain);
    }
}
