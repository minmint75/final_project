package com.example.final_project.service.serviceImpl;

import com.example.final_project.entity.ExamHistory;
import com.example.final_project.repository.ExamHistoryRepository;
import com.example.final_project.service.ExamHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamHistoryServiceImpl implements ExamHistoryService {

    private final ExamHistoryRepository examHistoryRepository;

    @Override
    public ExamHistory save(ExamHistory examHistory) {
        return examHistoryRepository.save(examHistory);
    }

    @Override
    public List<ExamHistory> getHistoriesByStudent(Long studentId) {
        return examHistoryRepository.findByStudentIdOrderBySubmittedAtDesc(studentId);
    }

    @Override
    public List<ExamHistory> getHistoriesByExam(Long examId) {
        return examHistoryRepository.findByExamIdOrderBySubmittedAtDesc(examId);
    }

    @Override
    public List<ExamHistory> getHistoriesByExamAndStudent(Long examId, Long studentId) {
        return examHistoryRepository.findByExamIdAndStudentIdOrderBySubmittedAtDesc(examId, studentId);
    }

    @Override
    public Long getTotalAttempts(Long examId) {
        return examHistoryRepository.countAttemptsByExam(examId);
    }

    @Override
    public List<Object[]> getExamRanking() {
        return examHistoryRepository.getExamRanking();
    }
}
