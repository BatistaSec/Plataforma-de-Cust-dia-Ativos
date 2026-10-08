package com.custody.custody_service.domain.exceptions;

public class PortfolioAlreadyExistsException extends BusinessException {

    public PortfolioAlreadyExistsException(String message) {
        super(message);
    }
}
