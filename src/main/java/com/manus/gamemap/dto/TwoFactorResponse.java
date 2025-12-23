package com.manus.gamemap.dto;

public class TwoFactorResponse {
    private boolean required;
    private String message;
    private String tempToken; // Temporary token to identify user during 2FA step

    public TwoFactorResponse(boolean required, String message, String tempToken) {
        this.required = required;
        this.message = message;
        this.tempToken = tempToken;
    }

    public boolean isRequired() { return required; }
    public String getMessage() { return message; }
    public String getTempToken() { return tempToken; }
}
