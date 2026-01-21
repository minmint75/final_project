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

        private final ExamSessionRepository examSessionRepository;
        private final EntityDtoMapper entityDtoMapper;

        @Override
        @Transactional
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

                List<ExamSession> sessions = examSessionRepository.findByStudentAndExam(student, exam);
                if (sessions.isEmpty()) {
                        ExamSession newSession = ExamSession.builder()
                                        .student(student)
                                        .exam(exam)
                                        .startedAt(LocalDateTime.now())
                                        .build();
                        examSessionRepository.save(newSession);
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
                // --- ANTI-CHEAT: Calculate Real Time Spent ---
                // --- ANTI-CHEAT: Calculate Real Time Spent ---
                List<ExamSession> sessions = examSessionRepository.findByStudentAndExam(student, exam);
                long serverTimeSpent = 0;

                if (!sessions.isEmpty()) {
                        // Use the earliest session if multiple found (safest for anti-cheat)
                        ExamSession session = sessions.get(0);

                        java.time.Duration duration = java.time.Duration.between(session.getStartedAt(),
                                        LocalDateTime.now());
                        serverTimeSpent = duration.getSeconds();

                        examHistory.setStartedAt(session.getStartedAt()); // Save startedAt from session

                        // Clean up ALL sessions for this attempt
                        examSessionRepository.deleteAll(sessions);
                } else {
                        // Fallback if no session found (should unlikely happen if logic is correct)
                        serverTimeSpent = submissionDto.getTimeSpent() != null ? submissionDto.getTimeSpent() : 0;
                        examHistory.setStartedAt(LocalDateTime.now().minusSeconds(serverTimeSpent)); // Estimate
                                                                                                     // startedAt
                }

                // Allow a small buffer (e.g. 10 seconds) for latency
                // Or mostly trust server time. Let's trust server time primarily,
                // but if serverTime is unreasonably huge (e.g. user left tab open for days),
                // it might skew stats. However, for "Time Spent", keeping real time is correct.

                examHistory.setTimeSpent(serverTimeSpent);
                // --------------------------------------------

                List<ExamHistoryDetail> details = new java.util.ArrayList<>();
                for (Question question : questions) {
                        Long questionId = question.getId();
                        List<Long> studentSelectedIds = submittedAnswers.getOrDefault(questionId, List.of());
                        List<Long> correctIds = correctAnswersMap.getOrDefault(questionId, List.of());

                        for (Long selectedId : studentSelectedIds) {
                                boolean isAnswerCorrect = correctIds.contains(selectedId);
                                ExamHistoryDetail detail = new ExamHistoryDetail();
                                detail.setExamHistory(examHistory);
                                detail.setQuestionId(questionId);
                                detail.setAnswerId(selectedId);
                                detail.setCorrect(isAnswerCorrect);
                                details.add(detail);
                        }
                }
                examHistory.setDetails(details);

                ExamHistory savedHistory = examHistoryRepository.save(examHistory);

                return entityDtoMapper.toExamResultResponseDto(savedHistory);
        }
}
