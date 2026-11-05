package com.custody.custody_service.domain.models;

import com.custody.custody_service.domain.enums.EntryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LedgerEntry — immutability and precision")
class LedgerEntryTest {

    @Test
    @DisplayName("Construction with all fields sets values correctly")
    void constructionWithAllFields() {
        UUID id = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        BigDecimal quantity = new BigDecimal("50.00001");
        LocalDateTime createdAt = LocalDateTime.now();

        LedgerEntry entry = new LedgerEntry(id, transactionId, accountId, assetId,
                EntryType.CREDIT, quantity, createdAt);

        assertEquals(id, entry.getId());
        assertEquals(transactionId, entry.getTransactionId());
        assertEquals(accountId, entry.getAccountId());
        assertEquals(assetId, entry.getAssetId());
        assertEquals(EntryType.CREDIT, entry.getEntryType());
        assertEquals(0, quantity.compareTo(entry.getQuantity()));
        assertEquals(createdAt, entry.getCreatedAt());
    }

    @Test
    @DisplayName("Quantity preserves 5 decimal places")
    void quantityPreservesFiveDecimalPlaces() {
        BigDecimal qty = new BigDecimal("0.00001");
        LedgerEntry entry = new LedgerEntry(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), EntryType.DEBIT, qty, LocalDateTime.now());
        assertEquals(0, qty.compareTo(entry.getQuantity()));
        assertEquals(5, entry.getQuantity().scale());
    }

    @Test
    @DisplayName("LedgerEntry has no public setter methods")
    void noPublicSetters() {
        for (Method method : LedgerEntry.class.getDeclaredMethods()) {
            if (method.getName().startsWith("set") && Modifier.isPublic(method.getModifiers())) {
                fail("LedgerEntry should not have public setters, found: " + method.getName());
            }
        }
    }
}
