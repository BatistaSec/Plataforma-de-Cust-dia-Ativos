package com.custody.custody_service.domain.exceptions;

public class IdempotencyKeyConflictException extends BusinessException {

    public IdempotencyKeyConflictException(String message) {
        super(message);
    }
}
