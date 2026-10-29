package com.custody.auth_service.presentation.response;

public class AuthResponse {

    private String token;
    private String userId;

    public AuthResponse() {}

    public AuthResponse(String token, String userId) {
        this.token = token;
        this.userId = userId;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
