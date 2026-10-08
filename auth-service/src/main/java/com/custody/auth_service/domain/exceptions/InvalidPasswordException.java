package com.custody.auth_service.domain.exceptions;

public class InvalidPasswordException extends BusinessException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}
