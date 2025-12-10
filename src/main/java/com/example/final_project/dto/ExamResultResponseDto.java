package com.example.final_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResultResponseDto {
    private Long examHistoryId;
    private Long examId;
    private String examTitle;
    private Double score;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer totalQuestions;
    private LocalDateTime submittedAt;
    private java.util.List<StudentAnswerDto> studentAnswers;
    private String studentName;
    private String studentEmail;
    private Integer attemptNumber;
    private String categoryName;
    private boolean passed;
}
