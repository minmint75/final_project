package com.example.final_project.controller;

import com.example.final_project.dto.ExamRequestDto;
import com.example.final_project.entity.Exam;
import com.example.final_project.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class ExamController {

    private final ExamService examService;

    @PostMapping
    public ResponseEntity<Exam> createExam(@Valid @RequestBody ExamRequestDto dto, Principal principal) {
        Long teacherId = Long.parseLong(principal.getName()); // giả sử principal trả teacherId
        Exam exam = examService.createExam(dto, teacherId);
        return ResponseEntity.ok(exam);
    }

    @PutMapping("/{examId}")
    public ResponseEntity<Exam> updateExam(
            @PathVariable Long examId,
            @Valid @RequestBody ExamRequestDto dto,
            Principal principal) {
        Long teacherId = Long.parseLong(principal.getName());
        Exam exam = examService.updateExam(examId, dto, teacherId);
        return ResponseEntity.ok(exam);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Exam>> getMyExams(Principal principal) {
        Long teacherId = Long.parseLong(principal.getName());
        List<Exam> exams = examService.getExamsByTeacher(teacherId);
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/{examId}")
    public ResponseEntity<Exam> getExam(@PathVariable Long examId, Principal principal) {
        Long teacherId = Long.parseLong(principal.getName());
        Exam exam = examService.getExamById(examId, teacherId);
        return ResponseEntity.ok(exam);
    }
}