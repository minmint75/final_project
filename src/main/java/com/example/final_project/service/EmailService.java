package com.example.final_project.service;

public interface EmailService {
    void sendRegistrationSuccessEmail(String to);
    void sendTeacherApprovalEmail(String to);
    void sendTeacherRejectionEmail(String to);
}
