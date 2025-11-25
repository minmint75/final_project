package com.example.final_project.service;

import com.example.final_project.dto.ChangePasswordRequest;
import org.springframework.security.core.Authentication;

public interface PasswordService {
    void changePassword(ChangePasswordRequest request, Authentication authentication);
}
