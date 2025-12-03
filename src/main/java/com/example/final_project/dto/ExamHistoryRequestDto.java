package com.example.final_project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamHistoryRequestDto {
    private Long examId;
    private Long examOnlineId;
    private Double score;
    private Integer correctCount;
    private Integer wrongCount;
}
