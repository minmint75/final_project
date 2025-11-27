package com.example.final_project.controller;

import com.example.final_project.dto.*;
import com.example.final_project.entity.Student;
import com.example.final_project.entity.Teacher;
import com.example.final_project.service.FileStorageService;
import com.example.final_project.service.ProfileService;
import com.example.final_project.service.TeacherService;
import com.example.final_project.service.StudentService;
import com.example.final_project.service.QuestionService;
import com.example.final_project.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private EmailService emailService;

    @GetMapping("")
    public ResponseEntity<String> adminDashboard() {
        return ResponseEntity.ok("Welcome, Admin! (Đây là API response)");
    }

    @GetMapping("/accounts/teachers")
    public List<Teacher> getAllTeachers() {
        return teacherService.findAll();
    }

    @DeleteMapping("/accounts/teachers/{id}")
    public ResponseEntity<Void> deleteTeachers(@PathVariable Long id) {
        teacherService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accounts/teachers/{id}/lock")
    public ResponseEntity<?> lockTeacher(@PathVariable Long id) {
        return updateTeacherStatusInternal(id, Teacher.TeacherStatus.LOCKED);
    }

    @PostMapping("/accounts/teachers/{id}/unlock")
    public ResponseEntity<?> unlockTeacher(@PathVariable Long id) {
        return updateTeacherStatusInternal(id, Teacher.TeacherStatus.APPROVED);
    }

    @GetMapping("/accounts/students")
    public List<Student> getAllStudents() {
        return studentService.findAll();
    }

    @DeleteMapping("/accounts/students/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accounts/students/{id}/lock")
    public ResponseEntity<?> lockStudent(@PathVariable Long id) {
        return updateStudentStatusInternal(id, Student.StudentStatus.LOCKED);
    }

    @PostMapping("/accounts/students/{id}/unlock")
    public ResponseEntity<?> unlockStudent(@PathVariable Long id) {
        return updateStudentStatusInternal(id, Student.StudentStatus.ACTIVE);
    }

    @GetMapping("/teachers/pending")
    public ResponseEntity<List<Teacher>> getPendingTeachers() {
        return ResponseEntity.ok(teacherService.getPendingTeachers());
    }

    @PostMapping("/teachers/{teacherId}/approve")
    public ResponseEntity<String> approveTeacher(@PathVariable Long teacherId) {
        teacherService.approveTeacher(teacherId);
        return ResponseEntity.ok("Teacher approved successfully.");
    }

    @PostMapping("/teachers/{teacherId}/reject")
    public ResponseEntity<String> rejectTeacher(@PathVariable Long teacherId) {
        teacherService.rejectTeacher(teacherId);
        return ResponseEntity.ok("Teacher rejected successfully.");
    }

    private ResponseEntity<?> updateStudentStatusInternal(Long id, Student.StudentStatus status) {
        try {
            Student student = studentService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            
            Student.StudentStatus oldStatus = student.getStatus();
            student.setStatus(status);
            studentService.save(student);
            
            // Gửi email thông báo khi thay đổi trạng thái
            if (oldStatus != status) {
                if (status == Student.StudentStatus.LOCKED) {
                    emailService.sendAccountLockedEmail(
                        student.getEmail(), 
                        "học viên", 
                        student.getUsername() != null ? student.getUsername() : student.getEmail()
                    );
                } else if (status == Student.StudentStatus.ACTIVE && oldStatus == Student.StudentStatus.LOCKED) {
                    emailService.sendAccountUnlockedEmail(
                        student.getEmail(), 
                        "học viên", 
                        student.getUsername() != null ? student.getUsername() : student.getEmail()
                    );
                }
            }
            
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    private ResponseEntity<?> updateTeacherStatusInternal(Long id, Teacher.TeacherStatus status) {
        try {
            Teacher teacher = teacherService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            
            Teacher.TeacherStatus oldStatus = teacher.getStatus();
            teacher.setStatus(status);
            teacherService.save(teacher);
            
            // Gửi email thông báo khi thay đổi trạng thái
            if (oldStatus != status) {
                if (status == Teacher.TeacherStatus.LOCKED) {
                    emailService.sendAccountLockedEmail(
                        teacher.getEmail(), 
                        "giáo viên", 
                        teacher.getUsername() != null ? teacher.getUsername() : teacher.getEmail()
                    );
                } else if (status == Teacher.TeacherStatus.APPROVED && oldStatus == Teacher.TeacherStatus.LOCKED) {
                    emailService.sendAccountUnlockedEmail(
                        teacher.getEmail(), 
                        "giáo viên", 
                        teacher.getUsername() != null ? teacher.getUsername() : teacher.getEmail()
                    );
                }
            }
            
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/profile/upload-avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            String filename = fileStorageService.save(file);
            String fileUrl = "/uploads/" + filename;
            return ResponseEntity.ok(Map.of("avatarUrl", fileUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Could not upload the file: " + e.getMessage()));
        }
    }

    @PutMapping("/profile/update")
    public ResponseEntity<String> updateProfile(@RequestBody ProfileUpdateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            profileService.updateProfile(currentUsername, request);
            return ResponseEntity.ok("Profile updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin Question Management - Full permissions
    @GetMapping("/questions")
    public ResponseEntity<Page<QuestionResponseDto>> getAllQuestions(@RequestParam(defaultValue = "0") int page) {
        Page<QuestionResponseDto> questions = questionService.getAllQuestions(page, 10);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/questions/{id}")
    public ResponseEntity<QuestionResponseDto> getQuestion(@PathVariable Long id) {
        // Admin có quyền xem chi tiết bất kỳ câu hỏi nào
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    @PostMapping("/questions")
    public ResponseEntity<QuestionResponseDto> createQuestion(@Valid @RequestBody QuestionCreateDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not authenticated");
        }
        dto.setCreatedBy(auth.getName());
        QuestionResponseDto question = questionService.createQuestion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(question);
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<QuestionResponseDto> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionUpdateDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not authenticated");
        }
        QuestionResponseDto updated = questionService.updateQuestionAsAdmin(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        // Admin can delete any question without ownership check
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not authenticated");
        }
        questionService.deleteQuestionAsAdmin(id);
        return ResponseEntity.noContent().build();
    }
}