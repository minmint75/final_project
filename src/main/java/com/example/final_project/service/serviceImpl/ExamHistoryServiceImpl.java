package com.example.final_project.service.serviceImpl;

import com.example.final_project.entity.ExamHistory;
import com.example.final_project.repository.ExamHistoryRepository;
import com.example.final_project.service.ExamHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamHistoryServiceImpl implements ExamHistoryService {

    private final ExamHistoryRepository examHistoryRepository;

    @Override
    @Transactional
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

    @Override
    @Transactional(readOnly = true)
    public ExamHistory getById(Long id) {
        return examHistoryRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch sử bài thi với ID: " + id));
    }

    @Override
    public Integer countByStudentAndExam(Long studentId, Long examId) {
        return examHistoryRepository.countByStudentStudentIdAndExamExamId(studentId, examId);
    }

    @Override
    public Integer countByStudentAndExamOnline(Long studentId, Long examOnlineId) {
        return examHistoryRepository.countByStudentStudentIdAndExamOnlineId(studentId, examOnlineId);
    }

    @Override
    public List<Object[]> getExamRankingByExamId(Long examId) {
        return examHistoryRepository.findTopScoresByExamId(examId);
    }
}
