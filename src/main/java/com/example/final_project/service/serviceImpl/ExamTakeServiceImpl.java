package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.*;
import com.example.final_project.entity.*;
import com.example.final_project.mapper.EntityDtoMapper;
import com.example.final_project.repository.*;
import com.example.final_project.service.ExamTakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamTakeServiceImpl implements ExamTakeService {

    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;
    private final ExamHistoryRepository examHistoryRepository;
    private final QuestionRepository questionRepository;
    private final EntityDtoMapper entityDtoMapper;


    @Override
    @Transactional(readOnly = true)
    public ExamTakeResponseDto startExam(Long examId, Long studentId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài thi không tồn tại"));

        if (exam.getStartTime() != null && exam.getStartTime().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bài thi chưa bắt đầu");
        }

        if (exam.getEndTime() != null && exam.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bài thi đã kết thúc");
        }

        // TODO: Check if student has already taken the exam (if needed based on rules)

        List<QuestionTakeDto> questionDtos = exam.getExamQuestions().stream()
                .map(ExamQuestion::getQuestion)
                .map(entityDtoMapper::toQuestionTakeDto)
                .collect(Collectors.toList());

        return new ExamTakeResponseDto(
                exam.getExamId(),
                exam.getTitle(),
                exam.getDescription(),
                exam.getDurationMinutes(),
                exam.getStartTime(),
                exam.getEndTime(),
                questionDtos
        );
    }

    @Override
    @Transactional
    public ExamResultResponseDto submitExam(ExamSubmissionRequestDto submissionDto, Long studentId) {
        Exam exam = examRepository.findById(submissionDto.getExamId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài thi không tồn tại"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sinh viên không tồn tại"));

        Map<Long, Long> submittedAnswers = submissionDto.getAnswers();

        List<Question> questions = exam.getExamQuestions().stream()
                .map(ExamQuestion::getQuestion)
                .collect(Collectors.toList());

        Map<Long, Answer> correctAnswers = questions.stream()
                .flatMap(q -> q.getAnswers().stream())
                .filter(Answer::isCorrect)
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), Function.identity()));

        int correctCount = 0;
        for (Map.Entry<Long, Long> entry : submittedAnswers.entrySet()) {
            Long questionId = entry.getKey();
            Long submittedAnswerId = entry.getValue();

            Answer correctAnswer = correctAnswers.get(questionId);
            if (correctAnswer != null && correctAnswer.getId().equals(submittedAnswerId)) {
                correctCount++;
            }
        }

        int totalQuestions = questions.size();
        double score = (double) correctCount / totalQuestions * 100;
        int wrongCount = totalQuestions - correctCount;

        ExamHistory examHistory = new ExamHistory();
        examHistory.setExam(exam);
        examHistory.setExamTitle(exam.getTitle());
        examHistory.setTotalQuestions(totalQuestions);
        examHistory.setDifficulty(exam.getCategory().getCategoryName());
        examHistory.setStudent(student);
        examHistory.setDisplayName(student.getUsername());
        examHistory.setScore(score);
        examHistory.setCorrectCount(correctCount);
        examHistory.setWrongCount(wrongCount);
        examHistory.setSubmittedAt(LocalDateTime.now());

        ExamHistory savedHistory = examHistoryRepository.save(examHistory);

        return entityDtoMapper.toExamResultResponseDto(savedHistory);
    }
}
