package com.example.final_project.controller;

import com.example.final_project.dto.*;
import com.example.final_project.service.CustomUserDetails;
import com.example.final_project.service.ExamOnlineService;
import com.example.final_project.util.QrCodeUtil;
import com.google.zxing.WriterException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/online-exams")
@RequiredArgsConstructor
public class ExamOnlineController {

    private final ExamOnlineService examOnlineService;
    private final QrCodeUtil qrCodeUtil;

    @PostMapping("/create")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ExamOnlineResponse> createExamOnline(@Valid @RequestBody ExamOnlineRequest request, Principal principal) {
        ExamOnlineResponse response = examOnlineService.createExamOnline(request, (Authentication) principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/join/{accessCode}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamOnlineJoinResponse> joinExamOnline(@PathVariable String accessCode, Principal principal) {
        ExamOnlineJoinResponse response = examOnlineService.joinExamOnline(accessCode, (Authentication) principal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accessCode}/take")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamTakeResponseDto> takeExamOnline(@PathVariable String accessCode, Principal principal) {
        ExamTakeResponseDto exam = examOnlineService.getTakeExamOnline(accessCode, (Authentication) principal);
        return ResponseEntity.ok(exam);
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamResultResponseDto> submitExamOnline(@Valid @RequestBody ExamSubmissionOnlineDto submissionDto, Principal principal) {
        ExamResultResponseDto result = examOnlineService.submitOnlineExam(submissionDto, (Authentication) principal);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{accessCode}/qrcode")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String accessCode) throws IOException, WriterException {
        String url = examOnlineService.getWaitingRoomUrl(accessCode);
        byte[] qrCode = qrCodeUtil.generateQrCode(url, 250, 250);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrCode);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
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

    @GetMapping("/get/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ExamOnlineResponse> getExamOnlineById(@PathVariable Long id, Principal principal) {
        ExamOnlineResponse exam = examOnlineService.getExamOnlineById(id, (Authentication) principal);
        return ResponseEntity.ok(exam);
    }

    @PutMapping("/{id}/update")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ExamOnlineResponse> updateExamOnline(@PathVariable Long id, @Valid @RequestBody ExamOnlineRequest request, Principal principal) {
        ExamOnlineResponse response = examOnlineService.updateExamOnline(id, request, (Authentication) principal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ExamOnlineResponse> startExamOnline(@PathVariable Long id, Principal principal) {
        ExamOnlineResponse response = examOnlineService.startExamOnline(id, (Authentication) principal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/finish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ExamOnlineResponse> finishExamOnline(@PathVariable Long id, Principal principal) {
        ExamOnlineResponse response = examOnlineService.finishExamOnline(id, (Authentication) principal);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/delete")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteExamOnline(@PathVariable Long id, Principal principal) {
        examOnlineService.deleteExamOnlineById(id, (Authentication) principal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/results")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
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
