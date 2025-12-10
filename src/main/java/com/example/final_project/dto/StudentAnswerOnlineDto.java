package com.example.final_project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswerOnlineDto {
    @NotNull
    private Long questionId;

    private List<Long> answerOptionIds;

    private String answerText;
}
