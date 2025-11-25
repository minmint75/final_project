package com.example.final_project.service;

import com.example.final_project.dto.QuestionCreateDto;
import com.example.final_project.dto.QuestionUpdateDto;
import com.example.final_project.entity.Question;
import org.springframework.data.domain.Page;

public interface QuestionService {
    Question createQuestion(QuestionCreateDto dto);
    Question updateQuestion(Long id, QuestionUpdateDto dto, String actorUsername);
    Question getQuestionById(Long id);
    Page<Question> getAllQuestions(int page, int size);
    void deleteQuestion(Long id, String actorUsername);
}
