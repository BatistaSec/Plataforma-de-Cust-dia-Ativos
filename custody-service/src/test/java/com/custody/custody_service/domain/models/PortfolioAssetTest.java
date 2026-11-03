package com.custody.custody_service.domain.models;

import com.custody.custody_service.domain.exceptions.InsufficientBalanceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PortfolioAsset — debit/credit domain logic")
class PortfolioAssetTest {

    private PortfolioAsset createAsset(String quantity) {
        Portfolio portfolio = new Portfolio(UUID.randomUUID(), UUID.randomUUID());
        Asset asset = new Asset(UUID.randomUUID(), "PETR4", "Petrobras");
        return new PortfolioAsset(UUID.randomUUID(), portfolio, asset, new BigDecimal(quantity), 1L);
    }

    @Test
    @DisplayName("debit with sufficient balance decrements correctly")
    void debitWithSufficientBalance() {
        PortfolioAsset pa = createAsset("100");
        pa.debit(new BigDecimal("50"));
        assertEquals(new BigDecimal("50"), pa.getQuantity());
    }

    @Test
    @DisplayName("debit with exact balance leaves zero")
    void debitWithExactBalance() {
        PortfolioAsset pa = createAsset("100");
        pa.debit(new BigDecimal("100"));
        assertEquals(BigDecimal.ZERO, pa.getQuantity());
    }

    @Test
    @DisplayName("debit with insufficient balance throws InsufficientBalanceException")
    void debitWithInsufficientBalance() {
        PortfolioAsset pa = createAsset("100");
        assertThrows(InsufficientBalanceException.class, () -> pa.debit(new BigDecimal("150")));
    }

    @Test
    @DisplayName("debit with zero balance throws InsufficientBalanceException")
    void debitWithZeroBalance() {
        PortfolioAsset pa = createAsset("0");
        assertThrows(InsufficientBalanceException.class, () -> pa.debit(new BigDecimal("1")));
    }

    @Test
    @DisplayName("credit adds correctly")
    void creditAddsCorrectly() {
        PortfolioAsset pa = createAsset("50");
        pa.credit(new BigDecimal("30"));
        assertEquals(new BigDecimal("80"), pa.getQuantity());
    }

    @Test
    @DisplayName("quantity is never negative after successful debit")
    void quantityNeverNegativeAfterSuccessfulDebit() {
        PortfolioAsset pa = createAsset("75");
        pa.debit(new BigDecimal("75"));
        assertTrue(pa.getQuantity().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    @DisplayName("debit preserves decimal precision")
    void debitPreservesDecimalPrecision() {
        PortfolioAsset pa = createAsset("1.00003");
        pa.debit(new BigDecimal("0.00001"));
        assertEquals(new BigDecimal("1.00002"), pa.getQuantity());
    }
}
