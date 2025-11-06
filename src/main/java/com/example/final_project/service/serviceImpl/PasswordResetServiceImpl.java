package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.PasswordResetDto;
import com.example.final_project.entity.PasswordResetToken;
import com.example.final_project.entity.Student;
import com.example.final_project.entity.Teacher;
import com.example.final_project.repository.PasswordResetTokenRepository;
import com.example.final_project.repository.StudentRepository;
import com.example.final_project.repository.TeacherRepository;
import com.example.final_project.service.EmailService;
import com.example.final_project.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import java.util.Random;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void createPasswordResetOtpForUser(String email) {
        Optional<Student> studentOpt = studentRepository.findByEmail(email);
        Optional<Teacher> teacherOpt = teacherRepository.findByEmail(email);

        if (studentOpt.isEmpty() && teacherOpt.isEmpty()) {
            throw new RuntimeException("User with this email not found");
        }

        String token = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

        PasswordResetToken myToken = new PasswordResetToken(token, email, expiryDate);
        tokenRepository.save(myToken);

        emailService.sendPasswordResetEmail(email, token);
    }

    @Override
    public void validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);

        if (resetToken == null) {
            throw new RuntimeException("Invalid token");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Token has expired");
        }
    }

    @Override
    public void resetPassword(PasswordResetDto passwordResetDto) {
        validateToken(passwordResetDto.getToken());

        if (!passwordResetDto.getNewPassword().equals(passwordResetDto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(passwordResetDto.getToken());
        Optional<Student> studentOpt = studentRepository.findByEmail(resetToken.getEmail());
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            if (passwordEncoder.matches(passwordResetDto.getNewPassword(), student.getPassword())) {
                throw new RuntimeException("New password cannot be the same as the old password");
            }
            student.setPassword(passwordEncoder.encode(passwordResetDto.getNewPassword()));
            studentRepository.save(student);
            tokenRepository.delete(resetToken);
            return;
        }

        Optional<Teacher> teacherOpt = teacherRepository.findByEmail(resetToken.getEmail());
        if (teacherOpt.isPresent()) {
            Teacher teacher = teacherOpt.get();
            if (passwordEncoder.matches(passwordResetDto.getNewPassword(), teacher.getPassword())) {
                throw new RuntimeException("New password cannot be the same as the old password");
            }
            teacher.setPassword(passwordEncoder.encode(passwordResetDto.getNewPassword()));
            teacherRepository.save(teacher);
            tokenRepository.delete(resetToken);
            return;
        }

        throw new RuntimeException("User not found for the token");
    }
}