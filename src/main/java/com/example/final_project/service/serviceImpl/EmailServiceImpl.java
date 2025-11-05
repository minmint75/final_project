package com.example.final_project.service.serviceImpl;

import com.example.final_project.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendRegistrationSuccessEmail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Registration Successful");
        message.setText("Welcome! Your registration was successful.");
        mailSender.send(message);
    }

    @Override
    public void sendTeacherApprovalEmail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Teacher Account Approved");
        message.setText("Congratulations! Your teacher account has been approved by the admin.");
        mailSender.send(message);
    }

    @Override
    public void sendTeacherRejectionEmail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Teacher Account Rejected");
        message.setText("We regret to inform you that your teacher account registration has been rejected.");
        mailSender.send(message);
    }
}
