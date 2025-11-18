package com.example.final_project.controller;

import com.example.final_project.dto.ProfileUpdateRequest;
import com.example.final_project.entity.Teacher;
import com.example.final_project.service.FileStorageService;
import com.example.final_project.service.ProfileService;
import com.example.final_project.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/")
    public ResponseEntity<String> adminDashboard() {
        return ResponseEntity.ok("Welcome, Admin! (Đây là API response)");
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
}