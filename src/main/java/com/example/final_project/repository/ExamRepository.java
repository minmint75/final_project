package com.example.final_project.repository;

import com.example.final_project.entity.Exam;
import com.example.final_project.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    @Query("SELECT e FROM Exam e WHERE e.teacher.teacherId = :teacherId ORDER BY e.createdAt DESC")
    List<Exam> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    @Query("SELECT count(h) > 0 FROM ExamHistory h WHERE h.exam.examId = :examId")
    boolean hasSubmissions(Long examId);

    @Query("SELECT DISTINCT e FROM Exam e " +
           "LEFT JOIN FETCH e.examQuestions eq " +
           "LEFT JOIN FETCH eq.question " +
           "LEFT JOIN FETCH e.category " +
           "WHERE e.examId = :examId")
    Optional<Exam> findByIdWithQuestions(@Param("examId") Long examId);
    
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.answers " +
           "WHERE q.id IN :questionIds")
    List<Question> findByIdWithAnswers(@Param("questionIds") List<Long> questionIds);

    void deleteByExamId(Long examId);
}