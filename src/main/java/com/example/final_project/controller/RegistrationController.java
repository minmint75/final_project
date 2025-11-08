package com.example.final_project.controller;

import com.example.final_project.dto.RegistrationRequest;
import com.example.final_project.service.RegistrationService;
import com.example.final_project.repository.StudentRepository;
import com.example.final_project.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationRequest request) {
        try {
            // Validate request
            if (request == null) {
                return ResponseEntity.badRequest().body("Yêu cầu đăng ký không hợp lệ");
            }
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email không được để trống");
            }
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên đăng nhập không được để trống");
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Mật khẩu không được để trống");
            }
            if (request.getRole() == null) {
                return ResponseEntity.badRequest().body("Vải trò không được để trống");
            }
            
            String email = request.getEmail().trim();
            if (email != null) {
                // Check Student
                if (studentRepository.findByEmail(email).isPresent()) {
                    return ResponseEntity.badRequest().body("Email đã được sử dụng. Vui lòng dùng email khác.");
                }
                
                // Check Teacher with status-specific messages
                var existingTeacher = teacherRepository.findByEmail(email);
                if (existingTeacher.isPresent()) {
                    var teacher = existingTeacher.get();
                    if (teacher.getStatus() != null) {
                        switch (teacher.getStatus()) {
                            case PENDING:
                                return ResponseEntity.badRequest().body("Tài khoản giáo viên này đang chờ phê duyệt. Vui lòng đợi hoặc liên hệ quản trị viên.");
                            case REJECTED:
                                return ResponseEntity.badRequest().body("Email này đã bị từ chối. Vui lòng sử dụng email khác.");
                            case APPROVED:
                                return ResponseEntity.badRequest().body("Email đã được sử dụng. Vui lòng dùng email khác.");
                        }
                    }
                }
            }

            registrationService.register(request);
            return ResponseEntity.ok("Registration successful!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi đăng ký: " + e.getMessage());
        }
    }

}
