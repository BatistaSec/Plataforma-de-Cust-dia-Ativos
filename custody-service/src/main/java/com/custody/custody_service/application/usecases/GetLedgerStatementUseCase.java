package com.custody.custody_service.application.usecases;

import com.custody.custody_service.application.dto.LedgerEntryOutput;
import com.custody.custody_service.application.dto.LedgerStatementOutput;
import com.custody.custody_service.domain.models.LedgerEntry;
import com.custody.custody_service.domain.repositories.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetLedgerStatementUseCase {

    private final LedgerEntryRepository ledgerEntryRepository;

    public GetLedgerStatementUseCase(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(readOnly = true)
    public LedgerStatementOutput execute(UUID portfolioId, UUID assetId, int page, int size) {
        if (size > 100) size = 100;
        if (size < 1) size = 20;
        if (page < 0) page = 0;

        int offset = page * size;

        List<LedgerEntry> entries = ledgerEntryRepository.findByAccountIdAndAssetId(portfolioId, assetId, offset, size);
        long total = ledgerEntryRepository.countByAccountIdAndAssetId(portfolioId, assetId);
        int totalPages = (int) Math.ceil((double) total / size);

        List<LedgerEntryOutput> outputs = entries.stream()
                .map(e -> new LedgerEntryOutput(
                        e.getId(), e.getTransactionId(), e.getAccountId(), e.getAssetId(),
                        e.getEntryType(), e.getQuantity(), e.getCreatedAt()))
                .collect(Collectors.toList());

        return new LedgerStatementOutput(outputs, page, size, total, totalPages);
    }
}
