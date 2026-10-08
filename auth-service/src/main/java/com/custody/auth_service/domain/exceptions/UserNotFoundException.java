package com.custody.auth_service.domain.exceptions;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
