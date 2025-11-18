package com.example.final_project.repository;

import com.example.final_project.entity.ExamHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExamHistoryRepository extends JpaRepository<ExamHistory, Long> {

    @Query("SELECT eh FROM ExamHistory eh WHERE eh.student.studentId = :studentId ORDER BY eh.submittedAt DESC")
    List<ExamHistory> findByStudentIdOrderBySubmittedAtDesc(Long studentId);

    @Query("SELECT eh FROM ExamHistory eh WHERE eh.exam.examId = :examId ORDER BY eh.submittedAt DESC")
    List<ExamHistory> findByExamIdOrderBySubmittedAtDesc(Long examId);

    @Query("SELECT eh FROM ExamHistory eh WHERE eh.exam.examId = :examId AND eh.student.studentId = :studentId ORDER BY eh.submittedAt DESC")
    List<ExamHistory> findByExamIdAndStudentIdOrderBySubmittedAtDesc(Long examId, Long studentId);

    // Tổng số lượt thi của 1 bài thi
    @Query("SELECT COUNT(eh) FROM ExamHistory eh WHERE eh.exam.examId = :examId")
    Long countAttemptsByExam(Long examId);

    // Danh sách bài thi theo số lượng người thi nhiều nhất (yêu cầu 53)
    @Query("""
        SELECT eh.exam.examId, eh.examTitle, COUNT(eh)
        FROM ExamHistory eh
        GROUP BY eh.exam.examId, eh.examTitle
        ORDER BY COUNT(eh) DESC
    """)
    List<Object[]> getExamRanking();
}
