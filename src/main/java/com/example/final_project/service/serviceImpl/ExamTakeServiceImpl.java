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
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Bài thi không tồn tại"));

                if (exam.getStartTime() != null && exam.getStartTime().isAfter(LocalDateTime.now())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bài thi chưa bắt đầu");
                }

                if (exam.getEndTime() != null && exam.getEndTime().isBefore(LocalDateTime.now())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bài thi đã kết thúc");
                }

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
                                questionDtos);
        }

        @Override
        @Transactional
        public ExamResultResponseDto submitExam(ExamSubmissionRequestDto submissionDto, Long studentId) {
                Exam exam = examRepository.findById(submissionDto.getExamId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Bài thi không tồn tại"));

                Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Sinh viên không tồn tại"));

                Map<Long, List<Long>> submittedAnswers = submissionDto.getAnswers();

                List<Question> questions = exam.getExamQuestions().stream()
                                .map(ExamQuestion::getQuestion)
                                .collect(Collectors.toList());

                Map<Long, List<Long>> correctAnswersMap = questions.stream()
                                .collect(Collectors.toMap(
                                                Question::getId,
                                                q -> q.getAnswers().stream()
                                                                .filter(Answer::isCorrect)
                                                                .map(Answer::getId)
                                                                .collect(Collectors.toList())));

                int correctCount = 0;

                for (Question question : questions) {
                        Long questionId = question.getId();
                        List<Long> studentSelectedIds = submittedAnswers.getOrDefault(questionId, List.of());
                        List<Long> correctIds = correctAnswersMap.getOrDefault(questionId, List.of());

                        boolean isCorrect = studentSelectedIds.size() == correctIds.size()
                                        && studentSelectedIds.containsAll(correctIds)
                                        && correctIds.containsAll(studentSelectedIds);

                        if (isCorrect) {
                                correctCount++;
                        }
                }

                int totalQuestions = questions.size();
                double rawScore = (double) correctCount / totalQuestions * 10;
                double score = Math.round(rawScore * 10.0) / 10.0;
                int wrongCount = totalQuestions - correctCount;

                // Calculate attempt number
                Integer currentAttempts = examHistoryRepository.countByStudentStudentIdAndExamExamId(studentId,
                                exam.getExamId());
                int attemptNumber = (currentAttempts != null ? currentAttempts : 0) + 1;

                ExamHistory examHistory = new ExamHistory();
                examHistory.setExam(exam);
                examHistory.setExamTitle(exam.getTitle());
                examHistory.setTotalQuestions(totalQuestions);
                examHistory.setDifficulty(exam.getCategory().getName());
                examHistory.setStudent(student);
                examHistory.setDisplayName(student.getUsername());
                examHistory.setScore(score);
                examHistory.setCorrectCount(correctCount);
                examHistory.setWrongCount(wrongCount);
                examHistory.setSubmittedAt(LocalDateTime.now());
                examHistory.setAttemptNumber(attemptNumber);

                ExamHistory savedHistory = examHistoryRepository.save(examHistory);

                return entityDtoMapper.toExamResultResponseDto(savedHistory);
        }
}
