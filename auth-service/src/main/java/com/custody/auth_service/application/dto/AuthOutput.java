package com.custody.auth_service.application.dto;

public class AuthOutput {

    private final String token;

    public AuthOutput(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
}
