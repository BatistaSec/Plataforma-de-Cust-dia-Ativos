package com.custody.custody_service.domain.models;

import com.custody.custody_service.domain.enums.TransactionStatus;
import com.custody.custody_service.domain.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable domain model grouping one or more LedgerEntry records as a single
 * financial transaction (e.g. an order, a transfer, an adjustment).
 */
public class LedgerTransaction {

    private final UUID id;
    private final UUID reference;
    private final TransactionType transactionType;
    private final TransactionStatus status;
    private final LocalDateTime createdAt;

    public LedgerTransaction(UUID id, UUID reference, TransactionType transactionType,
                              TransactionStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.reference = reference;
        this.transactionType = transactionType;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getReference() { return reference; }
    public TransactionType getTransactionType() { return transactionType; }
    public TransactionStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
