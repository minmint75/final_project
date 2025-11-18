package com.example.final_project.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamHistoryDto {

    private Long id;

    // Thông tin bài thi
    private Long examId;
    private String examTitle;
    private Integer totalQuestions;
    private String difficulty;

    // Thông tin học viên
    private Long studentId;
    private String displayName;

    // Kết quả làm bài
    private Double score;
    private Integer correctCount;
    private Integer wrongCount;
    private LocalDateTime submittedAt;
    private Integer attemptNumber;
}
