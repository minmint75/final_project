package com.example.final_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveProgressDto {
    private Long studentId;
    private String displayName;
    private String avatarUrl;
    private Integer questionsAnswered;
    private Integer totalQuestions;
    private Double currentScore;
    private Long timeSpent; // Seconds
}
