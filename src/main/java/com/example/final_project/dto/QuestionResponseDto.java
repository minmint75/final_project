package com.example.final_project.dto;

import com.example.final_project.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponseDto {
    private Long id;
    private String title;
    private QuestionType type;
    private String difficulty;
    private CategoryListDto category;
    private List<AnswerDto> answers;
    private String createdBy;
    private LocalDateTime createdAt;
    private String categoryName;
    private String correctAnswer;
}
