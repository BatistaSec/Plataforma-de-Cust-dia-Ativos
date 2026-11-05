package com.custody.custody_service.domain.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IdempotencyRecord — immutability")
class IdempotencyRecordTest {

    @Test
    @DisplayName("Construction with all fields sets values correctly")
    void constructionWithAllFields() {
        UUID id = UUID.randomUUID();
        UUID key = UUID.randomUUID();
        String hash = "abc123";
        String snapshot = "{\"id\":\"test\"}";
        LocalDateTime createdAt = LocalDateTime.now();

        IdempotencyRecord record = new IdempotencyRecord(id, key, hash, snapshot, createdAt);

        assertEquals(id, record.getId());
        assertEquals(key, record.getIdempotencyKey());
        assertEquals(hash, record.getRequestHash());
        assertEquals(snapshot, record.getResponseSnapshot());
        assertEquals(createdAt, record.getCreatedAt());
    }

    @Test
    @DisplayName("IdempotencyRecord has no public setter methods")
    void noPublicSetters() {
        for (Method method : IdempotencyRecord.class.getDeclaredMethods()) {
            if (method.getName().startsWith("set") && Modifier.isPublic(method.getModifiers())) {
                fail("IdempotencyRecord should not have public setters, found: " + method.getName());
            }
        }
    }
}
