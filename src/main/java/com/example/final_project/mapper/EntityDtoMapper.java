package com.example.final_project.mapper;

import com.example.final_project.dto.*;
import com.example.final_project.entity.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityDtoMapper {

    public QuestionResponseDto toQuestionResponseDto(Question question) {
        if (question == null) {
            return null;
        }

        QuestionResponseDto dto = new QuestionResponseDto();
        dto.setId(question.getId());
        dto.setTitle(question.getTitle());
        dto.setType(question.getType());
        dto.setVisibility(question.getVisibility());
        dto.setDifficulty(question.getDifficulty());
        dto.setCreatedBy(question.getCreatedBy());
        dto.setCreatedAt(question.getCreatedAt());

        if (question.getCategory() != null) {
            dto.setCategory(toCategoryListDto(question.getCategory()));
            dto.setCategoryName(question.getCategory().getName());
        }

        dto.setCorrectAnswer(question.getCorrectAnswer());

        if (question.getAnswers() != null) {
            List<AnswerDto> answerDtos = question.getAnswers().stream().map(this::toAnswerDto)
                    .collect(Collectors.toList());
            dto.setAnswers(answerDtos);
        }

        return dto;
    }

    public AnswerDto toAnswerDto(Answer answer) {
        if (answer == null) {
            return null;
        }
        return new AnswerDto(answer.getId(), answer.getText(), answer.isCorrect());
    }

    public CategoryListDto toCategoryListDto(Category category) {
        if (category == null) {
            return null;
        }
        CategoryListDto dto = new CategoryListDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setCreatedByRole(category.getCreatedByRole());
        dto.setCreatedByName(category.getCreatedByName());
        return dto;
    }

    public TeacherResponseDto toTeacherResponseDto(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        return new TeacherResponseDto(teacher.getTeacherId(), teacher.getUsername(), teacher.getEmail(),
                teacher.getAvatar());
    }

    public ExamQuestionResponseDto toExamQuestionResponseDto(ExamQuestion examQuestion) {
        if (examQuestion == null) {
            return null;
        }
        ExamQuestionResponseDto dto = new ExamQuestionResponseDto();
        dto.setExamQuestionId(examQuestion.getExamQuestionId());
        dto.setOrderIndex(examQuestion.getOrderIndex());
        dto.setQuestion(toQuestionResponseDto(examQuestion.getQuestion()));
        return dto;
    }

    public ExamResponseDto toExamResponseDto(Exam exam) {
        if (exam == null) {
            return null;
        }

        List<ExamQuestionResponseDto> examQuestionDtos = exam.getExamQuestions() == null ? Collections.emptyList()
                : exam.getExamQuestions().stream().map(this::toExamQuestionResponseDto).collect(Collectors.toList());

        return ExamResponseDto.builder()
                .examId(exam.getExamId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .durationMinutes(exam.getDurationMinutes())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .teacher(toTeacherResponseDto(exam.getTeacher()))
                .category(toCategoryListDto(exam.getCategory()))
                .examQuestions(examQuestionDtos)
                .questionCount(examQuestionDtos.size())
                .build();
    }

    public AnswerOptionDto toAnswerOptionDto(Answer answer) {
        if (answer == null) {
            return null;
        }
        return new AnswerOptionDto(answer.getId(), answer.getText());
    }

    public QuestionTakeDto toQuestionTakeDto(Question question) {
        if (question == null) {
            return null;
        }
        List<AnswerOptionDto> answerOptions = question.getAnswers().stream()
                .map(this::toAnswerOptionDto)
                .collect(Collectors.toList());
        return new QuestionTakeDto(question.getId(), question.getTitle(), answerOptions);
    }

    public ExamResultResponseDto toExamResultResponseDto(ExamHistory examHistory) {
        if (examHistory == null) {
            return null;
        }
        return ExamResultResponseDto.builder()
                .examHistoryId(examHistory.getId())
                .examId(examHistory.getExam().getExamId())
                .examTitle(examHistory.getExamTitle())
                .score(examHistory.getScore())
                .correctCount(examHistory.getCorrectCount())
                .wrongCount(examHistory.getWrongCount())
                .totalQuestions(examHistory.getTotalQuestions())
                .submittedAt(examHistory.getSubmittedAt())
                .build();
    }
}
