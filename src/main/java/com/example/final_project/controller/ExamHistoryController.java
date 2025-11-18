package com.example.final_project.controller;

import com.example.final_project.entity.ExamHistory;
import com.example.final_project.service.ExamHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-history")
@RequiredArgsConstructor
public class ExamHistoryController {

    private final ExamHistoryService examHistoryService;

    // Lưu lịch sử bài thi
    @PostMapping
    public ExamHistory save(@RequestBody ExamHistory examHistory) {
        return examHistoryService.save(examHistory);
    }

    // Lấy lịch sử theo bài thi
    @GetMapping("/exam/{examId}")
    public List<ExamHistory> getByExam(@PathVariable Long examId) {
        return examHistoryService.getHistoriesByExam(examId);
    }

    // Lấy lịch sử theo học viên
    @GetMapping("/student/{studentId}")
    public List<ExamHistory> getByStudent(@PathVariable Long studentId) {
        return examHistoryService.getHistoriesByStudent(studentId);
    }

    // Lấy lịch sử theo bài thi + học viên
    @GetMapping("/{examId}/student/{studentId}")
    public List<ExamHistory> getByExamAndStudent(
            @PathVariable Long examId,
            @PathVariable Long studentId) {
        return examHistoryService.getHistoriesByExamAndStudent(examId, studentId);
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
