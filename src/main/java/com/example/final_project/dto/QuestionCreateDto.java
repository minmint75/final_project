package com.example.final_project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCreateDto {
    @NotBlank
    private String title;

    @NotBlank
    private String type; // SINGLE, MULTIPLE, TRUE_FALSE

    @NotBlank
    private String difficulty; // "Khó", "Trung bình", "Dễ"

    @NotNull
    private Long categoryId;

    @NotBlank
    private String createdBy;

    @NotEmpty
    @Valid
    private List<AnswerDto> answers;
}