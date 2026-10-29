package com.custody.auth_service.application.dto;

public class AuthOutput {

    private final String token;
    private final String userId;

    public AuthOutput(String token, String userId) {
        this.token = token;
        this.userId = userId;
    }

    public String getToken() { return token; }
    public String getUserId() { return userId; }
}
