package com.example.final_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionRequestDto {
    private Long examId;
    private Long timeSpent;
    private Map<Long, List<Long>> answers; // Key: questionId, Value: List of answerIds
}
