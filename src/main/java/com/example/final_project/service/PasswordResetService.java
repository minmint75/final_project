package com.example.final_project.service;

import com.example.final_project.dto.PasswordResetDto;

public interface PasswordResetService {
    String createPasswordResetOtpForUser(String email);
    void validateToken(String token);
    void resetPassword(PasswordResetDto passwordResetDto);
}
