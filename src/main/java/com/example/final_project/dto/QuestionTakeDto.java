package com.example.final_project.dto;

import com.example.final_project.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTakeDto {
    private Long id;
    private String text;
    private QuestionType type;
    private List<AnswerOptionDto> answers;
}
