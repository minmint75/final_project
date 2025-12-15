package com.example.final_project.repository;

import com.example.final_project.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long>, JpaSpecificationExecutor<Exam> {

    @Query("SELECT e FROM Exam e WHERE e.teacher.teacherId = :teacherId ORDER BY e.createdAt DESC")
    List<Exam> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    @Query("SELECT count(h) > 0 FROM ExamHistory h WHERE h.exam.examId = :examId")
    boolean hasSubmissions(Long examId);

    boolean existsByTitle(String title);

    Optional<Exam> findByCode(String code);

    void deleteByExamId(Long examId);
}
