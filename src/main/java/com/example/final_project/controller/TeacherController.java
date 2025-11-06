package com.example.final_project.controller;

import com.example.final_project.dto.TeacherSearchRequest;
import com.example.final_project.entity.Teacher;
import com.example.final_project.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @GetMapping
    public Page<Teacher> getAllTeachers(TeacherSearchRequest request) {
        return teacherService.searchTeachers(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacherById(@PathVariable Long id) {
        Optional<Teacher> teacher = teacherService.findById(id);
        return teacher.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Teacher createTeacher(@RequestBody Teacher teacher) {
        return teacherService.save(teacher);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Teacher> updateTeacher(@PathVariable Long id, @RequestBody Teacher teacherDetails) {
        Optional<Teacher> teacherOptional = teacherService.findById(id);
        if (teacherOptional.isPresent()) {
            Teacher teacher = teacherOptional.get();
            teacher.setUsername(teacherDetails.getUsername());
            teacher.setEmail(teacherDetails.getEmail());
            teacher.setPassword(teacherDetails.getPassword());
            return ResponseEntity.ok(teacherService.save(teacher));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Teacher> approveTeacher(@PathVariable Long id) {
        Optional<Teacher> teacherOptional = teacherService.findById(id);
        if (teacherOptional.isPresent()) {
            Teacher teacher = teacherOptional.get();
            teacher.setStatus(Teacher.TeacherStatus.APPROVED);
            return ResponseEntity.ok(teacherService.save(teacher));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Teacher> rejectTeacher(@PathVariable Long id) {
        Optional<Teacher> teacherOptional = teacherService.findById(id);
        if (teacherOptional.isPresent()) {
            Teacher teacher = teacherOptional.get();
            teacher.setStatus(Teacher.TeacherStatus.REJECTED);
            return ResponseEntity.ok(teacherService.save(teacher));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}