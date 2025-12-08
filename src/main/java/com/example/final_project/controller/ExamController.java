package com.example.final_project.controller;

import com.example.final_project.dto.ExamRequestDto;
import com.example.final_project.dto.ExamResponseDto;
import com.example.final_project.service.CustomUserDetails;
import com.example.final_project.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @PostMapping("/create")
    public ResponseEntity<ExamResponseDto> createExam(@Valid @RequestBody ExamRequestDto dto, Principal principal) {
        Long userId = getAuthenticatedUserId(principal);
        ExamResponseDto exam = examService.createExam(dto, userId);
        return ResponseEntity.ok(exam);
    }

    @PutMapping("/edit/{examId}")
    public ResponseEntity<ExamResponseDto> updateExam(
            @PathVariable Long examId,
            @Valid @RequestBody ExamRequestDto dto,
            Principal principal) {
        Long userId = getAuthenticatedUserId(principal);
        ExamResponseDto exam = examService.updateExam(examId, dto, userId);
        return ResponseEntity.ok(exam);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ExamResponseDto>> getAllExams(Pageable pageable) {
        Page<ExamResponseDto> exams = examService.getAllExams(pageable);
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ExamResponseDto>> getMyExams(Principal principal) {
        Authentication authentication = (Authentication) principal;
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.ok(examService.getAllExams(Pageable.unpaged()).getContent());
        } else {
            Long teacherId = getAuthenticatedUserId(principal);
            List<ExamResponseDto> exams = examService.getExamsByTeacher(teacherId);
            return ResponseEntity.ok(exams);
        }
    }

    @GetMapping("/get/{examId}")
    public ResponseEntity<ExamResponseDto> getExam(@PathVariable Long examId, Principal principal) {
        Long userId = getAuthenticatedUserId(principal);
        ExamResponseDto exam = examService.getExamById(examId, userId);
        return ResponseEntity.ok(exam);
    }

    @DeleteMapping("/delete/{examId}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long examId, Principal principal) {
        Long userId = getAuthenticatedUserId(principal);
        try {
            examService.deleteExamById(examId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            // Can be more specific with custom exceptions
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    private Long getAuthenticatedUserId(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác thực được người dùng");
        }
        Authentication authentication = (Authentication) principal;
        Object principalObject = authentication.getPrincipal();
        if (principalObject instanceof CustomUserDetails) {
            return ((CustomUserDetails) principalObject).getId();
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Loại principal người dùng không hợp lệ. Không phải CustomUserDetails.");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ExamResponseDto>> searchExams(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) com.example.final_project.entity.ExamLevel examLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        Long userId = getAuthenticatedUserId(principal);
        Authentication authentication = (Authentication) principal;
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        com.example.final_project.dto.ExamSearchRequest searchRequest = new com.example.final_project.dto.ExamSearchRequest();
        searchRequest.setTitle(title);
        searchRequest.setCategoryId(categoryId);
        searchRequest.setExamLevel(examLevel);

        // If not admin, restrict to own exams
        if (!isAdmin) {
            searchRequest.setTeacherId(userId);
        } else {
            // Admin can see everything
        }

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("createdAt").descending());
        return ResponseEntity.ok(examService.searchExams(searchRequest, pageable));
    }
}