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
        dto.setCreatedByName(question.getCreatedByName());
        dto.setCreatedByRole(question.getCreatedByRole());
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
        dto.setCreatedBy(category.getCreatedBy());
        return dto;
    }

    public TeacherResponseDto toTeacherResponseDto(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        return new TeacherResponseDto(teacher.getTeacherId(), teacher.getUsername(), teacher.getEmail(),
                teacher.getAvatar());
    }

    public StudentResponseDto toStudentResponseDto(Student student) {
        if (student == null) {
            return null;
        }
        return new StudentResponseDto(student.getStudentId(), student.getUsername(), student.getEmail(),
                student.getAvatar());
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
                .examQuestions(examQuestionDtos)
                .questionCount(examQuestionDtos.size())
                .examLevel(exam != null && exam.getExamLevel() != null ? exam.getExamLevel().name() : null)
                .status(exam != null && exam.getStatus() != null ? exam.getStatus().name() : null)
                .code(exam.getCode())
                .url(exam.getUrl())
                .isPrivate(exam.getStatus() == ExamStatus.PRIVATE)
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
        return new QuestionTakeDto(question.getId(), question.getTitle(), question.getType(), answerOptions);
    }

    public ExamResultResponseDto toExamResultResponseDto(ExamHistory examHistory) {
        if (examHistory == null) {
            return null;
        }

        List<StudentAnswerDto> answerDtos = null;
        if (examHistory.getDetails() != null) {
            answerDtos = examHistory.getDetails().stream()
                    .map(detail -> new StudentAnswerDto(detail.getQuestionId(), detail.getAnswerId(),
                            detail.isCorrect()))
                    .collect(Collectors.toList());
        }

        return ExamResultResponseDto.builder()
                .examHistoryId(examHistory.getId())
                .examId(examHistory.getExam() != null ? examHistory.getExam().getExamId() : null)
                .examOnlineId(examHistory.getExamOnline() != null ? examHistory.getExamOnline().getId() : null)
                .examTitle(examHistory.getExamTitle())
                .score(examHistory.getScore())
                .correctCount(examHistory.getCorrectCount())
                .wrongCount(examHistory.getWrongCount())
                .totalQuestions(examHistory.getTotalQuestions())
                .submittedAt(examHistory.getSubmittedAt())
                .studentAnswers(answerDtos)
                .studentName(examHistory.getDisplayName())
                .studentEmail(examHistory.getStudent() != null ? examHistory.getStudent().getEmail() : null)
                .attemptNumber(examHistory.getAttemptNumber())
                .categoryName(examHistory.getExam() != null && examHistory.getExam().getCategory() != null
                        ? examHistory.getExam().getCategory().getName()
                        : "N/A")
                .build();
    }

    public ExamHistoryResponseDto toExamHistoryResponseDto(ExamHistory examHistory) {
        if (examHistory == null) {
            return null;
        }

        ExamHistoryResponseDto dto = new ExamHistoryResponseDto();
        dto.setId(examHistory.getId());

        if (examHistory.getExam() != null) {
            dto.setExamId(examHistory.getExam().getExamId());
        }

        if (examHistory.getExamOnline() != null) {
            dto.setExamOnlineId(examHistory.getExamOnline().getId());
        }

        dto.setExamTitle(examHistory.getExamTitle());
        dto.setTotalQuestions(examHistory.getTotalQuestions());
        dto.setDifficulty(examHistory.getDifficulty());

        if (examHistory.getStudent() != null) {
            dto.setStudentId(examHistory.getStudent().getStudentId());
        }

        dto.setDisplayName(examHistory.getDisplayName());
        dto.setScore(examHistory.getScore());
        dto.setCorrectCount(examHistory.getCorrectCount());
        dto.setWrongCount(examHistory.getWrongCount());
        dto.setSubmittedAt(examHistory.getSubmittedAt());
        dto.setAttemptNumber(examHistory.getAttemptNumber());
        dto.setTimeSpent(examHistory.getTimeSpent());
        dto.setPassed(examHistory.getPassed());

        return dto;
    }
}
