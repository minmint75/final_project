package com.example.final_project.controller;

import com.example.final_project.dto.ChangePasswordRequest;
import com.example.final_project.entity.Admin;
import com.example.final_project.entity.Student;
import com.example.final_project.entity.Teacher;
import com.example.final_project.repository.AdminRepository;
import com.example.final_project.repository.StudentRepository;
import com.example.final_project.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ChangePasswordController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("message", "Bạn cần đăng nhập để thực hiện thao tác này");
                return ResponseEntity.status(401).body(response);
            }

            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty() ||
                request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                response.put("message", "Vui lòng nhập đầy đủ thông tin");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getNewPassword().length() < 6) {
                response.put("message", "Mật khẩu phải có ít nhất 6 ký tự");
                return ResponseEntity.badRequest().body(response);
            }

            String email = authentication.getName();

            // Try to find the current user among Admin, Teacher, Student by email
            Optional<Admin> adminOpt = adminRepository.findByEmail(email);
            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                if (!passwordEncoder.matches(request.getCurrentPassword(), admin.getPassword())) {
                    response.put("message", "Mật khẩu hiện tại không chính xác");
                    return ResponseEntity.badRequest().body(response);
                }
                if (passwordEncoder.matches(request.getNewPassword(), admin.getPassword())) {
                    response.put("message", "Mật khẩu mới không được trùng với mật khẩu hiện tại");
                    return ResponseEntity.badRequest().body(response);
                }
                admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
                adminRepository.save(admin);
                response.put("message", "Đổi mật khẩu thành công");
                return ResponseEntity.ok(response);
            }

            Optional<Teacher> teacherOpt = teacherRepository.findByEmail(email);
            if (teacherOpt.isPresent()) {
                Teacher teacher = teacherOpt.get();
                if (!passwordEncoder.matches(request.getCurrentPassword(), teacher.getPassword())) {
                    response.put("message", "Mật khẩu hiện tại không chính xác");
                    return ResponseEntity.badRequest().body(response);
                }
                if (passwordEncoder.matches(request.getNewPassword(), teacher.getPassword())) {
                    response.put("message", "Mật khẩu mới không được trùng với mật khẩu hiện tại");
                    return ResponseEntity.badRequest().body(response);
                }
                teacher.setPassword(passwordEncoder.encode(request.getNewPassword()));
                teacherRepository.save(teacher);
                response.put("message", "Đổi mật khẩu thành công");
                return ResponseEntity.ok(response);
            }

            Optional<Student> studentOpt = studentRepository.findByEmail(email);
            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                if (!passwordEncoder.matches(request.getCurrentPassword(), student.getPassword())) {
                    response.put("message", "Mật khẩu hiện tại không chính xác");
                    return ResponseEntity.badRequest().body(response);
                }
                if (passwordEncoder.matches(request.getNewPassword(), student.getPassword())) {
                    response.put("message", "Mật khẩu mới không được trùng với mật khẩu hiện tại");
                    return ResponseEntity.badRequest().body(response);
                }
                student.setPassword(passwordEncoder.encode(request.getNewPassword()));
                studentRepository.save(student);
                response.put("message", "Đổi mật khẩu thành công");
                return ResponseEntity.ok(response);
            }

            response.put("message", "Người dùng không tồn tại");
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Đã xảy ra lỗi khi đổi mật khẩu");
            return ResponseEntity.status(500).body(response);
        }
    }
}
