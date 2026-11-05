package com.custody.custody_service.presentation.response;

import com.custody.custody_service.domain.enums.EntryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class LedgerEntryResponse {

    private final UUID id;
    private final UUID transactionId;
    private final UUID accountId;
    private final UUID assetId;
    private final EntryType entryType;
    private final BigDecimal quantity;
    private final LocalDateTime createdAt;

    public LedgerEntryResponse(UUID id, UUID transactionId, UUID accountId, UUID assetId,
                                 EntryType entryType, BigDecimal quantity, LocalDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.assetId = assetId;
        this.entryType = entryType;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getTransactionId() { return transactionId; }
    public UUID getAccountId() { return accountId; }
    public UUID getAssetId() { return assetId; }
    public EntryType getEntryType() { return entryType; }
    public BigDecimal getQuantity() { return quantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
