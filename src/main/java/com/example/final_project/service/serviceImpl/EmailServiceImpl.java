package com.example.final_project.service.serviceImpl;

import com.example.final_project.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Override
    public void sendRegistrationSuccessEmail(String to) {
        if (!emailEnabled) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Registration Successful");
            message.setText("Welcome! Your registration was successful.");
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendTeacherPendingEmail(String to) {
        if (!emailEnabled) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Đơn đăng ký giáo viên đang chờ xem xét");
            message.setText("Xin chào,\n\nCảm ơn bạn đã đăng ký trở thành giáo viên trên QuizzZone. " +
                    "Tài khoản của bạn hiện đang chờ phê duyệt từ quản trị viên. " +
                    "Chúng tôi sẽ gửi cho bạn một email thông báo khi tài khoản của bạn được xem xét.\n\n" +
                    "Trân trọng,\n" +
                    "Nhóm QuizzZone");
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendTeacherApprovalEmail(String to) {
        if (!emailEnabled) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Tài khoản giáo viên đã được phê duyệt");
            message.setText("Xin chào,\n\nChúc mừng! Tài khoản giáo viên của bạn đã được phê duyệt bởi quản trị viên. " +
                    "Bạn hiện có thể đăng nhập và bắt đầu sử dụng QuizzZone.\n\n" +
                    "Trân trọng,\n" +
                    "Nhóm QuizzZone");
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendTeacherRejectionEmail(String to) {
        if (!emailEnabled) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Đơn đăng ký giáo viên bị từ chối");
            message.setText("Xin chào,\n\nRất tiếc, chúng tôi phải thông báo rằng đơn đăng ký giáo viên của bạn bị từ chối. " +
                    "Nếu bạn có câu hỏ, vui lòng liên hệ quản trị viên.\n\n" +
                    "Trân trọng,\n" +
                    "Nhóm QuizzZone");
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        if (!emailEnabled) return;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Password Reset Request");
            helper.setText("<html><body><p>Dear User,</p><p>You have requested to reset your password. Your One-Time Password (OTP) is: <strong>" + token + "</strong></p><p>This OTP is valid for 1 hour. If you did not request a password reset, please ignore this email.</p><p>Thank you,<br/>The Quiz App Team</p></body></html>", true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
