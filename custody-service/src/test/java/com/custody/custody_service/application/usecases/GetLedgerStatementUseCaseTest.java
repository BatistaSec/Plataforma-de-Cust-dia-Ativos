package com.custody.custody_service.application.usecases;

import com.custody.custody_service.application.dto.LedgerStatementOutput;
import com.custody.custody_service.domain.enums.EntryType;
import com.custody.custody_service.domain.models.LedgerEntry;
import com.custody.custody_service.domain.repositories.LedgerEntryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetLedgerStatementUseCase — unit tests")
class GetLedgerStatementUseCaseTest {

    @Mock private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks private GetLedgerStatementUseCase useCase;

    @Test
    @DisplayName("Statement with results returns paginated entries")
    void statementWithResults() {
        UUID portfolioId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        List<LedgerEntry> entries = List.of(
            new LedgerEntry(UUID.randomUUID(), txId, portfolioId, assetId,
                EntryType.CREDIT, new BigDecimal("100"), LocalDateTime.now()),
            new LedgerEntry(UUID.randomUUID(), txId, portfolioId, assetId,
                EntryType.DEBIT, new BigDecimal("30"), LocalDateTime.now())
        );

        when(ledgerEntryRepository.findByAccountIdAndAssetId(portfolioId, assetId, 0, 20))
                .thenReturn(entries);
        when(ledgerEntryRepository.countByAccountIdAndAssetId(portfolioId, assetId))
                .thenReturn(2L);

        LedgerStatementOutput output = useCase.execute(portfolioId, assetId, 0, 20);

        assertEquals(2, output.getEntries().size());
        assertEquals(0, output.getPage());
        assertEquals(20, output.getSize());
        assertEquals(2, output.getTotalElements());
        assertEquals(1, output.getTotalPages());
    }

    @Test
    @DisplayName("Statement empty returns 200 with empty list")
    void statementEmpty() {
        UUID portfolioId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        when(ledgerEntryRepository.findByAccountIdAndAssetId(portfolioId, assetId, 0, 20))
                .thenReturn(List.of());
        when(ledgerEntryRepository.countByAccountIdAndAssetId(portfolioId, assetId))
                .thenReturn(0L);

        LedgerStatementOutput output = useCase.execute(portfolioId, assetId, 0, 20);

        assertTrue(output.getEntries().isEmpty());
        assertEquals(0, output.getTotalElements());
        assertEquals(0, output.getTotalPages());
    }

    @Test
    @DisplayName("Statement without assetId returns all assets for portfolio")
    void statementWithoutAssetId() {
        UUID portfolioId = UUID.randomUUID();
        UUID asset1 = UUID.randomUUID();
        UUID asset2 = UUID.randomUUID();

        List<LedgerEntry> entries = List.of(
            new LedgerEntry(UUID.randomUUID(), UUID.randomUUID(), portfolioId, asset1,
                EntryType.CREDIT, new BigDecimal("50"), LocalDateTime.now()),
            new LedgerEntry(UUID.randomUUID(), UUID.randomUUID(), portfolioId, asset2,
                EntryType.CREDIT, new BigDecimal("30"), LocalDateTime.now())
        );

        when(ledgerEntryRepository.findByAccountIdAndAssetId(portfolioId, null, 0, 20))
                .thenReturn(entries);
        when(ledgerEntryRepository.countByAccountIdAndAssetId(portfolioId, null))
                .thenReturn(2L);

        LedgerStatementOutput output = useCase.execute(portfolioId, null, 0, 20);

        assertEquals(2, output.getEntries().size());
        assertEquals(2, output.getTotalElements());
    }

    @Test
    @DisplayName("Pagination caps size at 100")
    void paginationCapsSize() {
        UUID portfolioId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        when(ledgerEntryRepository.findByAccountIdAndAssetId(portfolioId, assetId, 0, 100))
                .thenReturn(List.of());
        when(ledgerEntryRepository.countByAccountIdAndAssetId(portfolioId, assetId))
                .thenReturn(0L);

        LedgerStatementOutput output = useCase.execute(portfolioId, assetId, 0, 200);

        assertEquals(100, output.getSize());
    }
}
