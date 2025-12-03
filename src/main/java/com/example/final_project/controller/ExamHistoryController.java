package com.example.final_project.controller;

import com.example.final_project.dto.ExamHistoryRequestDto;
import com.example.final_project.dto.ExamHistoryResponseDto;
import com.example.final_project.dto.ExamResultResponseDto;
import com.example.final_project.entity.Exam;
import com.example.final_project.entity.ExamHistory;
import com.example.final_project.entity.ExamOnline;
import com.example.final_project.entity.Student;
import com.example.final_project.mapper.EntityDtoMapper;
import com.example.final_project.repository.ExamOnlineRepository;
import com.example.final_project.repository.ExamRepository;
import com.example.final_project.repository.StudentRepository;
import com.example.final_project.service.CustomUserDetails;
import com.example.final_project.service.ExamHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/examHistory")
@RequiredArgsConstructor
public class ExamHistoryController {

    private final ExamHistoryService examHistoryService;
    private final EntityDtoMapper entityDtoMapper;
    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final ExamOnlineRepository examOnlineRepository;


    // Lưu lịch sử bài thi
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ExamHistoryResponseDto save(@RequestBody ExamHistoryRequestDto dto, Principal principal) {
        Long studentId = getAuthenticatedStudentId(principal);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        ExamHistory examHistory = new ExamHistory();
        examHistory.setStudent(student);
        examHistory.setDisplayName(student.getUsername());
        examHistory.setScore(dto.getScore());
        examHistory.setCorrectCount(dto.getCorrectCount());
        examHistory.setWrongCount(dto.getWrongCount());
        examHistory.setSubmittedAt(LocalDateTime.now());


        if (dto.getExamId() != null) {
            Exam exam = examRepository.findById(dto.getExamId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));
            examHistory.setExam(exam);
            examHistory.setExamTitle(exam.getTitle());
            examHistory.setTotalQuestions(exam.getExamQuestions().size());
            examHistory.setDifficulty(exam.getExamLevel().toString());

            // Tính số lần thử
            Integer attemptNumber = examHistoryService.countByStudentAndExam(studentId, dto.getExamId()) + 1;
            examHistory.setAttemptNumber(attemptNumber);


        } else if (dto.getExamOnlineId() != null) {
            ExamOnline examOnline = examOnlineRepository.findById(dto.getExamOnlineId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ExamOnline not found"));
            examHistory.setExamOnline(examOnline);
            examHistory.setExamTitle(examOnline.getName());
            examHistory.setTotalQuestions(examOnline.getQuestions().size());
            examHistory.setDifficulty(examOnline.getLevel().toString());

            // Tính số lần thử
            Integer attemptNumber = examHistoryService.countByStudentAndExamOnline(studentId, dto.getExamOnlineId()) + 1;
            examHistory.setAttemptNumber(attemptNumber);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exam history must be associated with either an Exam (via examId) or an ExamOnline (via examOnlineId).");
        }

        ExamHistory savedExamHistory = examHistoryService.save(examHistory);
        return entityDtoMapper.toExamHistoryResponseDto(savedExamHistory);
    }

    // Lấy lịch sử theo bài thi
    @GetMapping("/get/{examId}")
    public List<ExamHistoryResponseDto> getByExam(@PathVariable Long examId) {
        return examHistoryService.getHistoriesByExam(examId).stream()
                .map(entityDtoMapper::toExamHistoryResponseDto)
                .collect(Collectors.toList());
    }

    // Lấy lịch sử theo học viên
    @GetMapping("/student/{studentId}")
    public List<ExamHistoryResponseDto> getByStudent(@PathVariable Long studentId) {
        return examHistoryService.getHistoriesByStudent(studentId).stream()
                .map(entityDtoMapper::toExamHistoryResponseDto)
                .collect(Collectors.toList());
    }

    // Lấy lịch sử theo bài thi + học viên
    @GetMapping("/{examId}/student/{studentId}")
    public List<ExamHistoryResponseDto> getByExamAndStudent(
            @PathVariable Long examId,
            @PathVariable Long studentId) {
        return examHistoryService.getHistoriesByExamAndStudent(examId, studentId).stream()
                .map(entityDtoMapper::toExamHistoryResponseDto)
                .collect(Collectors.toList());
    }

    // Xem chi tiết một lần làm bài
    @GetMapping("/detail/{historyId}")
    public ExamResultResponseDto getHistoryDetail(@PathVariable Long historyId) {
        ExamHistory history = examHistoryService.getById(historyId);
        return entityDtoMapper.toExamResultResponseDto(history);
    }

    // Lấy tổng số lượt thi 1 bài thi
    @GetMapping("/summary/{examId}")
    public Long getTotalAttempts(@PathVariable Long examId) {
        return examHistoryService.getTotalAttempts(examId);
    }

    // Lấy danh sách bài thi theo số lượt thi nhiều nhất
    @GetMapping("/ranking")
    public List<Object[]> getExamRanking() {
        return examHistoryService.getExamRanking();
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
