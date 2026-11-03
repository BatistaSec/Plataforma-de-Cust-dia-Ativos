package com.custody.custody_service.domain.models;

import com.custody.custody_service.domain.enums.MovementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AssetMovement — immutability and precision")
class AssetMovementTest {

    @Test
    @DisplayName("construction with all fields sets values correctly")
    void constructionWithAllFields() {
        UUID id = UUID.randomUUID();
        UUID portfolioId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID referenceOrderId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();
        BigDecimal quantity = new BigDecimal("50");

        AssetMovement movement = new AssetMovement(
                id, portfolioId, assetId, MovementType.CREDIT,
                quantity, referenceOrderId, occurredAt
        );

        assertEquals(id, movement.getId());
        assertEquals(portfolioId, movement.getPortfolioId());
        assertEquals(assetId, movement.getAssetId());
        assertEquals(MovementType.CREDIT, movement.getType());
        assertEquals(quantity, movement.getQuantity());
        assertEquals(referenceOrderId, movement.getReferenceOrderId());
        assertEquals(occurredAt, movement.getOccurredAt());
    }

    @Test
    @DisplayName("quantity preserves 5 decimal places without rounding")
    void quantityPreservesFiveDecimalPlaces() {
        BigDecimal qty = new BigDecimal("0.00001");
        AssetMovement movement = new AssetMovement(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                MovementType.DEBIT, qty,
                UUID.randomUUID(), LocalDateTime.now()
        );
        assertEquals(qty, movement.getQuantity());
        assertEquals(5, movement.getQuantity().scale());
    }

    @Test
    @DisplayName("AssetMovement has no public setter methods")
    void noPublicSetters() {
        Method[] methods = AssetMovement.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("set") && Modifier.isPublic(method.getModifiers())) {
                fail("AssetMovement should not have public setters, found: " + method.getName());
            }
        }
    }
}
