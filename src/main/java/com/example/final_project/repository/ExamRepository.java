package com.example.final_project.repository;

import com.example.final_project.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    @Query("SELECT e FROM Exam e WHERE e.teacher.teacherId = :teacherId ORDER BY e.createdAt DESC")
    List<Exam> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    boolean existsByExamIdAndExamQuestions_StudentAnswers_NotEmpty(Long examId);
}