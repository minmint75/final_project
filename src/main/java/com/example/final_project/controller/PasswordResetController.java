package com.example.final_project.controller;

import com.example.final_project.dto.PasswordResetDto;
import com.example.final_project.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Email is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String otp = passwordResetService.createPasswordResetOtpForUser(email);
            
            // OTP is no longer returned in the response for security reasons.
            Map<String, String> response = new HashMap<>();
            response.put("message", "OTP for password reset has been sent to your email.");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (token == null || token.trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Token is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            passwordResetService.validateToken(token);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Token is valid.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetDto passwordResetDto) {
        try {
            if (passwordResetDto.getToken() == null || passwordResetDto.getToken().trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Token is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (passwordResetDto.getNewPassword() == null || passwordResetDto.getNewPassword().trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "New password is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            passwordResetService.resetPassword(passwordResetDto);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password has been reset successfully.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    @GetMapping("/test")
            public String testEndpoint() {
                     // In ra để chúng ta biết nó đã được gọi
                     System.out.println(">>> /api/test endpoint was successfully hit! <<<");
                     return "Backend is reachable!";
                 }

}
