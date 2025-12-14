package com.example.final_project.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CodeGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public String generateUniqueCode() {
        byte[] randomBytes = new byte[6];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public String generateUrl(String code) {
        // This should be configured to point to the frontend URL for taking a private exam
        return "http://localhost:5173/exam/private/" + code;
    }
}
