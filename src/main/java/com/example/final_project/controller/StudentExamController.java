
package com.example.final_project.controller;

import com.example.final_project.dto.ExamResponseDto;
import com.example.final_project.dto.ExamSearchRequest;
import com.example.final_project.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.final_project.dto.JoinExamByCodeRequest;
import com.example.final_project.service.CustomUserDetails;
import jakarta.validation.Valid;

import java.security.Principal;

@RestController
@RequestMapping("/api/student/exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentExamController {

    private final ExamService examService;

    @GetMapping("/search")
    public ResponseEntity<Page<ExamResponseDto>> searchExams(ExamSearchRequest searchRequest, Pageable pageable, Principal principal) {
        Long studentId = ((CustomUserDetails) ((Authentication) principal).getPrincipal()).getId();
        Page<ExamResponseDto> exams = examService.searchExams(searchRequest, pageable, studentId, searchRequest.isIncludeAuthorizedPrivate());
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamResponseDto> getExamById(@org.springframework.web.bind.annotation.PathVariable Long id) {
        ExamResponseDto exam = examService.getExamForStudent(id);
        return ResponseEntity.ok(exam);
    }

    @PostMapping("/join-by-code")
    public ResponseEntity<ExamResponseDto> joinByCode(@Valid @RequestBody JoinExamByCodeRequest request, Principal principal) {
        Long studentId = ((CustomUserDetails) ((Authentication) principal).getPrincipal()).getId();
        ExamResponseDto exam = examService.joinOfflineExamByCode(request.getCode(), studentId);
        return ResponseEntity.ok(exam);
    }
}
