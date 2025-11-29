package com.example.final_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamHistoryDetailDto {
    private Long examHistoryId;
    private String examTitle;
    private String displayName;
    private Double score;
    private LocalDateTime submittedAt;
    private List<QuestionResultDto> questions;
}
