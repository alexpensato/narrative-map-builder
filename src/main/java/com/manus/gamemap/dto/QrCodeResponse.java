package com.manus.gamemap.dto;

public class QrCodeResponse {
    private String qrCodeUri;
    private String secret;

    public QrCodeResponse(String qrCodeUri, String secret) {
        this.qrCodeUri = qrCodeUri;
        this.secret = secret;
    }

    public String getQrCodeUri() { return qrCodeUri; }
    public String getSecret() { return secret; }
}
