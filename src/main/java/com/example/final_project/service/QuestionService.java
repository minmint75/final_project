package com.example.final_project.service;

import com.example.final_project.dto.QuestionCreateDto;
import com.example.final_project.dto.QuestionResponseDto;
import com.example.final_project.dto.QuestionUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionService {
    QuestionResponseDto createQuestion(QuestionCreateDto dto);

    QuestionResponseDto updateQuestion(Long id, QuestionUpdateDto dto, String actorUsername);

    QuestionResponseDto updateQuestionAsAdmin(Long id, QuestionUpdateDto dto);

    QuestionResponseDto getQuestionById(Long id);

    Page<QuestionResponseDto> getAllQuestions(int page, int size);

    Page<QuestionResponseDto> getQuestionsByUser(String username, int page, int size);

    Page<QuestionResponseDto> searchQuestions(String keyword, String difficulty, String type, Long categoryId,
            String createdBy, String visibility, String currentUsername, Pageable pageable);

    void deleteQuestion(Long id, String actorUsername);

    void deleteQuestionAsAdmin(Long id);
}
