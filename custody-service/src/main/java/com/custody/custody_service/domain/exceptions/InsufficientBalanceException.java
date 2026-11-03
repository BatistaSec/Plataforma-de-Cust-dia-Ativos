package com.custody.custody_service.domain.exceptions;

import java.math.BigDecimal;

public class InsufficientBalanceException extends BusinessException {

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(BigDecimal currentBalance, BigDecimal requestedQuantity) {
        super("Saldo insuficiente: portfólio possui " + currentBalance + " unidades, solicitado " + requestedQuantity);
    }
}
