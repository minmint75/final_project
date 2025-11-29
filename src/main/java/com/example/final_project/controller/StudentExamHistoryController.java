package com.example.final_project.controller;

import com.example.final_project.dto.ExamHistoryDetailDto;
import com.example.final_project.service.CustomUserDetails;
import com.example.final_project.service.ExamHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/api/student/exam-history")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentExamHistoryController {

    private final ExamHistoryService examHistoryService;

    @GetMapping("/{historyId}")
    public ResponseEntity<ExamHistoryDetailDto> getHistoryDetails(@PathVariable Long historyId, Principal principal) {
        Long studentId = getAuthenticatedStudentId(principal);
        ExamHistoryDetailDto historyDetails = examHistoryService.getExamHistoryDetails(historyId, studentId);
        return ResponseEntity.ok(historyDetails);
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
