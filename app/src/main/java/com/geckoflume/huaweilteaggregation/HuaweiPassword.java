package com.geckoflume.huaweilteaggregation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class HuaweiPassword {
    private static HuaweiPassword instance;
    private String pwd;

    private HuaweiPassword(String pwd, String csrf) {
        this.pwd = pwd;
        sha256hash();
        base64encode();
        this.pwd = "admin" + this.pwd + csrf;
        sha256hash();
        base64encode();
    }

    public static HuaweiPassword getInstance(String pwd, String csrf) {
        if (instance == null) {
            instance = new HuaweiPassword(pwd, csrf);
        }
        return instance;
    }

    private String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // Hash password using SHA256
    private void sha256hash() {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hashInBytes = digest.digest(pwd.getBytes(StandardCharsets.UTF_8));
            this.pwd = bytesToString(hashInBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // Encode password using Base64
    private void base64encode() {
        this.pwd = Base64.getEncoder().encodeToString(this.pwd.getBytes());
    }

    public String getPwd() {
        return pwd;
    }
}
