package com.example.final_project.service;

import com.example.final_project.entity.ExamHistory;

import java.util.List;

public interface ExamHistoryService {

    ExamHistory save(ExamHistory examHistory);

    List<ExamHistory> getHistoriesByStudent(Long studentId);

    List<ExamHistory> getHistoriesByExam(Long examId);

    List<ExamHistory> getHistoriesByExamAndStudent(Long examId, Long studentId);

    Long getTotalAttempts(Long examId);

    List<Object[]> getExamRanking();
}
