package com.example.final_project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionOnlineDto {
    @NotNull
    private String accessCode;

    @NotEmpty
    @Valid
    private List<StudentAnswerOnlineDto> answers;

    private Long timeSpent; // Time spent in seconds
}
