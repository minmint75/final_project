package com.example.final_project.dto;

import com.example.final_project.entity.ExamLevel;
import com.example.final_project.entity.ExamStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamOnlineResponse {
    private Long id;
    private String name;
    private Integer numberOfQuestions;
    private ExamLevel level;
    private LocalDateTime submissionDeadline;
    private Integer passingScore;
    private Integer maxParticipants;
    private String accessCode;
    private ExamStatus status;
    private String teacherName;
    private LocalDateTime createdAt;
    private Long categoryId;
    private String categoryName;
}
