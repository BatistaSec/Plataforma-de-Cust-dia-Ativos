package com.custody.custody_service.domain.models;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable domain model storing an idempotency key, the SHA-256 hash of the
 * original request body, and a snapshot of the response for replay.
 */
public class IdempotencyRecord {

    private final UUID id;
    private final UUID idempotencyKey;
    private final String requestHash;
    private final String responseSnapshot;
    private final LocalDateTime createdAt;

    public IdempotencyRecord(UUID id, UUID idempotencyKey, String requestHash,
                             String responseSnapshot, LocalDateTime createdAt) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.responseSnapshot = responseSnapshot;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getIdempotencyKey() { return idempotencyKey; }
    public String getRequestHash() { return requestHash; }
    public String getResponseSnapshot() { return responseSnapshot; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
