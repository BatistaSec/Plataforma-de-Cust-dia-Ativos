package com.custody.custody_service.domain.models;

import com.custody.custody_service.domain.enums.TransactionStatus;
import com.custody.custody_service.domain.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LedgerTransaction — immutability")
class LedgerTransactionTest {

    @Test
    @DisplayName("Construction with all fields sets values correctly")
    void constructionWithAllFields() {
        UUID id = UUID.randomUUID();
        UUID reference = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        LedgerTransaction tx = new LedgerTransaction(id, reference, TransactionType.ORDER,
                TransactionStatus.CONFIRMED, createdAt);

        assertEquals(id, tx.getId());
        assertEquals(reference, tx.getReference());
        assertEquals(TransactionType.ORDER, tx.getTransactionType());
        assertEquals(TransactionStatus.CONFIRMED, tx.getStatus());
        assertEquals(createdAt, tx.getCreatedAt());
    }

    @Test
    @DisplayName("LedgerTransaction has no public setter methods")
    void noPublicSetters() {
        for (Method method : LedgerTransaction.class.getDeclaredMethods()) {
            if (method.getName().startsWith("set") && Modifier.isPublic(method.getModifiers())) {
                fail("LedgerTransaction should not have public setters, found: " + method.getName());
            }
        }
    }
}
