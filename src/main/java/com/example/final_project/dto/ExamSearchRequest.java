package com.example.final_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamSearchRequest {
    private Long categoryId;
    private String title;
    private com.example.final_project.entity.ExamLevel examLevel;
    private Long teacherId;
    private com.example.final_project.entity.ExamStatus status;
}
