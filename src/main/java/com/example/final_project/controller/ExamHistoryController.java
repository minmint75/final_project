package com.example.final_project.controller;

import com.example.final_project.dto.ExamHistoryResponseDto;
import com.example.final_project.dto.ExamResultResponseDto;
import com.example.final_project.entity.ExamHistory;
import com.example.final_project.mapper.EntityDtoMapper;
import com.example.final_project.service.ExamHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/examHistory")
@RequiredArgsConstructor
public class ExamHistoryController {

    private final ExamHistoryService examHistoryService;
    private final EntityDtoMapper entityDtoMapper;

    // Lưu lịch sử bài thi
    @PostMapping
    public ExamHistory save(@RequestBody ExamHistory examHistory) {
        return examHistoryService.save(examHistory);
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
}
