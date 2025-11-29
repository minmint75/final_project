package com.example.final_project.controller;

import com.example.final_project.dto.ExamSubmissionRequestDto;
import com.example.final_project.dto.ExamResultResponseDto;
import com.example.final_project.dto.ExamTakeResponseDto;
import com.example.final_project.service.CustomUserDetails;
import com.example.final_project.service.ExamTakeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/api/student/exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class ExamTakeController {

    private final ExamTakeService examTakeService;

    @PostMapping("/{examId}/start")
    public ResponseEntity<ExamTakeResponseDto> startExam(@PathVariable Long examId, Principal principal) {
        Long studentId = getAuthenticatedStudentId(principal);
        ExamTakeResponseDto exam = examTakeService.startExam(examId, studentId);
        return ResponseEntity.ok(exam);
    }

    @PostMapping("/submit")
    public ResponseEntity<ExamResultResponseDto> submitExam(@Valid @RequestBody ExamSubmissionRequestDto submissionDto, Principal principal) {
        Long studentId = getAuthenticatedStudentId(principal);
        ExamResultResponseDto result = examTakeService.submitExam(submissionDto, studentId);
        return ResponseEntity.ok(result);
    }

    private Long getAuthenticatedStudentId(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác thực được người dùng");
        }
        Authentication authentication = (Authentication) principal;
        Object principalObject = authentication.getPrincipal();
        if (principalObject instanceof CustomUserDetails) {
            return ((CustomUserDetails) principalObject).getId();
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Loại principal người dùng không hợp lệ. Không phải CustomUserDetails.");
        }
    }
}
