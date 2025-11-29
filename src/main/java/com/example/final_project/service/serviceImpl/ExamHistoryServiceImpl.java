package com.example.final_project.service.serviceImpl;


import com.example.final_project.entity.Exam;
import com.example.final_project.entity.ExamHistory;
import com.example.final_project.entity.Student;
import com.example.final_project.repository.ExamHistoryRepository;
import com.example.final_project.repository.ExamRepository;
import com.example.final_project.repository.StudentRepository;
import com.example.final_project.service.ExamHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamHistoryServiceImpl implements ExamHistoryService {

    private final ExamHistoryRepository examHistoryRepository;
    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public ExamHistory save(ExamHistory examHistory) {
        // Lấy ID của Exam và Student từ đối tượng examHistory được truyền vào
        Long examId = examHistory.getExam().getExamId();
        Long studentId = examHistory.getStudent().getStudentId();

        // Tải các thực thể Exam và Student từ cơ sở dữ liệu
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi với ID: " + examId));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên với ID: " + studentId));

        // Thiết lập các thực thể đã được quản lý vào examHistory
        examHistory.setExam(exam);
        examHistory.setStudent(student);

        // Bây giờ lưu examHistory
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
