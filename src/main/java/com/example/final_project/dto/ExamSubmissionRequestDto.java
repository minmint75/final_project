package com.example.final_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionRequestDto {
    private Long examId;
    private Map<Long, Long> answers; // Key: questionId, Value: answerId
}
