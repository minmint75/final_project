package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.ChangePasswordRequest;
import com.example.final_project.entity.Admin;
import com.example.final_project.entity.Student;
import com.example.final_project.entity.Teacher;
import com.example.final_project.repository.AdminRepository;
import com.example.final_project.repository.StudentRepository;
import com.example.final_project.repository.TeacherRepository;
import com.example.final_project.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PasswordServiceImpl implements PasswordService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public void changePassword(ChangePasswordRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        String email = authentication.getName();

        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            validateAndupdatePassword(admin.getPassword(), request, password -> admin.setPassword(password));
            adminRepository.save(admin);
            return;
        }

        Optional<Teacher> teacherOpt = teacherRepository.findByEmail(email);
        if (teacherOpt.isPresent()) {
            Teacher teacher = teacherOpt.get();
            validateAndupdatePassword(teacher.getPassword(), request, password -> teacher.setPassword(password));
            teacherRepository.save(teacher);
            return;
        }

        Optional<Student> studentOpt = studentRepository.findByEmail(email);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            validateAndupdatePassword(student.getPassword(), request, password -> student.setPassword(password));
            studentRepository.save(student);
            return;
        }

        throw new IllegalStateException("User not found");
    }

    private void validateAndupdatePassword(String currentHashedPassword, ChangePasswordRequest request, PasswordSetter passwordSetter) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentHashedPassword)) {
            throw new IllegalArgumentException("Incorrect current password");
        }
        if (passwordEncoder.matches(request.getNewPassword(), currentHashedPassword)) {
            throw new IllegalArgumentException("New password cannot be the same as the old password");
        }
        passwordSetter.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @FunctionalInterface
    private interface PasswordSetter {
        void setPassword(String password);
    }
}
