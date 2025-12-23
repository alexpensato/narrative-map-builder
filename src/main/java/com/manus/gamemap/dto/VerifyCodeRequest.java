package com.manus.gamemap.dto;

public class VerifyCodeRequest {
    private String tempToken;
    private String code;

    public String getTempToken() { return tempToken; }
    public void setTempToken(String tempToken) { this.tempToken = tempToken; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
