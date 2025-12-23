package com.manus.gamemap.dto;

public class AuthResponse {
    private String token;
    private String username;
    private String role;
    private String sessionId;

    public AuthResponse(String token, String username, String role, String sessionId) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.sessionId = sessionId;
    }

    public String getToken() { return token; }
    public String getSessionId() { return sessionId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}
