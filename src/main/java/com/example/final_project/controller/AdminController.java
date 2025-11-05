package com.example.final_project.controller;

import com.example.final_project.entity.Teacher;
import com.example.final_project.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/dashboard")
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
}