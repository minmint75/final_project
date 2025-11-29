package com.example.final_project.controller;

import com.example.final_project.dto.ExamOnlineRequest;
import com.example.final_project.dto.ExamOnlineResponse;
import com.example.final_project.dto.ExamOnlineResultsDto;
import com.example.final_project.service.CustomUserDetails;
import com.example.final_project.service.ExamOnlineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/online-exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')") // Allow both TEACHER and ADMIN
public class ExamOnlineController {

    private final ExamOnlineService examOnlineService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')") // Allow both to create
    public ResponseEntity<ExamOnlineResponse> createExamOnline(@Valid @RequestBody ExamOnlineRequest request, Principal principal) {
        ExamOnlineResponse response = examOnlineService.createExamOnline(request, (Authentication) principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ExamOnlineResponse>> getMyOnlineExams(Principal principal) {
        Authentication authentication = (Authentication) principal;
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<ExamOnlineResponse> exams;
        if (isAdmin) {
            exams = examOnlineService.getAllOnlineExams();
        } else {
            Long teacherId = getAuthenticatedUserId(principal);
            exams = examOnlineService.getMyOnlineExams(teacherId);
        }
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamOnlineResponse> getExamOnlineById(@PathVariable Long id, Principal principal) {
        ExamOnlineResponse exam = examOnlineService.getExamOnlineById(id, (Authentication) principal);
        return ResponseEntity.ok(exam);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<ExamOnlineResponse> updateExamOnline(@PathVariable Long id, @Valid @RequestBody ExamOnlineRequest request, Principal principal) {
        ExamOnlineResponse response = examOnlineService.updateExamOnline(id, request, (Authentication) principal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ExamOnlineResponse> startExamOnline(@PathVariable Long id, Principal principal) {
        ExamOnlineResponse response = examOnlineService.startExamOnline(id, (Authentication) principal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<ExamOnlineResponse> finishExamOnline(@PathVariable Long id, Principal principal) {
        ExamOnlineResponse response = examOnlineService.finishExamOnline(id, (Authentication) principal);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteExamOnline(@PathVariable Long id, Principal principal) {
        examOnlineService.deleteExamOnlineById(id, (Authentication) principal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<ExamOnlineResultsDto> getExamOnlineResults(@PathVariable Long id, Principal principal) {
        ExamOnlineResultsDto results = examOnlineService.getExamOnlineResults(id, (Authentication) principal);
        return ResponseEntity.ok(results);
    }

    private Long getAuthenticatedUserId(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        Authentication authentication = (Authentication) principal;
        Object principalObject = authentication.getPrincipal();
        if (principalObject instanceof CustomUserDetails) {
            return ((CustomUserDetails) principalObject).getId();
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid user principal type");
        }
    }
}
