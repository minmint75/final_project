package com.example.final_project.repository;

import com.example.final_project.entity.Exam;
import com.example.final_project.entity.ExamSession;
import com.example.final_project.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    List<ExamSession> findByStudentAndExam(Student student, Exam exam);
}
