package com.example.final_project.controller;

import com.example.final_project.dto.ExamRequestDto;
import com.example.final_project.dto.ExamResponseDto;
import com.example.final_project.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
public class ExamController {

    private final ExamService examService;

    @PostMapping
    public ResponseEntity<ExamResponseDto> createExam(@Valid @RequestBody ExamRequestDto dto, Principal principal) {
        Long teacherId = getTeacherIdFromPrincipal(principal);
        ExamResponseDto exam = examService.createExam(dto, teacherId);
        return ResponseEntity.ok(exam);
    }

    @PutMapping("/update/{examId}")
    public ResponseEntity<ExamResponseDto> updateExam(
            @PathVariable Long examId,
            @Valid @RequestBody ExamRequestDto dto,
            Principal principal) {
        Long teacherId = getTeacherIdFromPrincipal(principal);
        ExamResponseDto exam = examService.updateExam(examId, dto, teacherId);
        return ResponseEntity.ok(exam);
    }

    @GetMapping
    public ResponseEntity<Page<ExamResponseDto>> getAllExams(Pageable pageable) {
        Page<ExamResponseDto> exams = examService.getAllExams(pageable);
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ExamResponseDto>> getMyExams(Principal principal) {
        Long teacherId = getTeacherIdFromPrincipal(principal);
        List<ExamResponseDto> exams = examService.getExamsByTeacher(teacherId);
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/{examId}")
    public ResponseEntity<ExamResponseDto> getExam(@PathVariable Long examId, Principal principal) {
        Long teacherId = getTeacherIdFromPrincipal(principal);
        ExamResponseDto exam = examService.getExamById(examId, teacherId);
        return ResponseEntity.ok(exam);
    }

    @DeleteMapping("/delete/{examId}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long examId, Principal principal) {
        Long teacherId = getTeacherIdFromPrincipal(principal);
        try {
            examService.deleteExamById(examId, teacherId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            // Can be more specific with custom exceptions
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    private Long getTeacherIdFromPrincipal(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác thực được người dùng");
        }
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ID người dùng không hợp lệ");
        }
    }
}