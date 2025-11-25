package com.example.final_project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionUpdateDto {
    @NotBlank
    private String title;

    @NotBlank
    private String type;

    @NotBlank
    private String difficulty;

    @NotNull
    private Long categoryId;

    @NotEmpty
    @Valid
    private List<AnswerDto> answers;

    private String correctAnswer;
}