package com.example.final_project.service;

import com.example.final_project.dto.QuestionCreateDto;
import com.example.final_project.dto.QuestionUpdateDto;
import com.example.final_project.entity.Question;
import org.springframework.data.domain.Page;

// Giả định bạn có một Exception chung cho Not Found
import java.util.NoSuchElementException;
public interface QuestionService {
    Question createQuestion(QuestionCreateDto dto);
    Question getQuestionById(Long id) throws NoSuchElementException;
    Page<Question> getAllQuestions(int page, int size);
    Page<Question> searchQuestions(int page, int size, String search, String difficulty, String type, String category);
    Question updateQuestion(Long id, QuestionUpdateDto dto, String actorUsername) throws SecurityException, NoSuchElementException;
    void deleteQuestion(Long id, String actorUsername) throws SecurityException, NoSuchElementException;
}
