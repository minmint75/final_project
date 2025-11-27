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

    @Override
    public void sendAccountLockedEmail(String toEmail, String accountType, String userName) {
        if (!emailEnabled) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Tài khoản " + accountType + " đã bị khóa");
            message.setText(buildAccountLockedEmail(accountType, userName));
            mailSender.send(message);
            System.out.println("Account locked email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send account locked email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendAccountUnlockedEmail(String toEmail, String accountType, String userName) {
        if (!emailEnabled) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Tài khoản " + accountType + " đã được mở khóa");
            message.setText(buildAccountUnlockedEmail(accountType, userName));
            mailSender.send(message);
            System.out.println("Account unlocked email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send account unlocked email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildAccountLockedEmail(String accountType, String userName) {
        return "Kính gửi " + userName + ",\n\n" +
               "Chúng tôi xin thông báo rằng tài khoản " + accountType + " của bạn đã bị khóa bởi quản trị viên.\n\n" +
               "Lý do khóa tài khoản:\n" +
               "- Vi phạm chính sách sử dụng hệ thống\n" +
               "- Hoạt động đáng ngờ được phát hiện\n" +
               "- Yêu cầu từ quản trị viên\n\n" +
               "Hậu quả của việc bị khóa:\n" +
               "- Bạn không thể đăng nhập vào hệ thống\n" +
               "- Không thể truy cập các tính năng của " + accountType + "\n" +
               "- Dữ liệu của bạn vẫn được bảo toàn\n\n" +
               "Để mở khóa tài khoản, vui lòng:\n" +
               "1. Liên hệ quản trị viên qua email: admin@quiz.edu.vn\n" +
               "2. Gọi hotline: 1900-1234\n" +
               "3. Hoặc đến trực tiếp văn phòng quản trị\n\n" +
               "Thời gian làm việc: 8:00 - 17:00 (Thứ 2 - Thứ 6)\n\n" +
               "Trân trọng,\n" +
               "Ban quản trị hệ thống Quiz\n\n" +
               "---\n" +
               "Đây là email tự động, vui lòng không trả lời email này.";
    }

    private String buildAccountUnlockedEmail(String accountType, String userName) {
        return "Kính gửi " + userName + ",\n\n" +
               "Chúng tôi xin thông báo rằng tài khoản " + accountType + " của bạn đã được mở khóa.\n\n" +
               "Thông tin mở khóa:\n" +
               "- Thời gian mở khóa: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n" +
               "- Người thực hiện: Quản trị viên hệ thống\n" +
               "- Trạng thái: Đã kích hoạt\n\n" +
               "Bạn có thể:\n" +
               "✅ Đăng nhập vào hệ thống bình thường\n" +
               "✅ Sử dụng đầy đủ tính năng của " + accountType + "\n" +
               "✅ Truy cập dữ liệu và tài nguyên của mình\n\n" +
               "Lưu ý:\n" +
               "- Vui lòng tuân thủ chính sách sử dụng hệ thống\n" +
               "- Bảo mật thông tin đăng nhập của bạn\n" +
               "- Liên hệ ngay nếu có hoạt động bất thường\n\n" +
               "Nếu có bất kỳ câu hỏi nào, vui lòng liên hệ:\n" +
               "📧 Email: admin@quiz.edu.vn\n" +
               "📞 Hotline: 1900-1234\n\n" +
               "Cảm ơn bạn đã sử dụng hệ thống Quiz!\n\n" +
               "Trân trọng,\n" +
               "Ban quản trị hệ thống Quiz\n\n" +
               "---\n" +
               "Đây là email tự động, vui lòng không trả lời email này.";
    }
}
