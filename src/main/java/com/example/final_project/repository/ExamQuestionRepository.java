package com.example.final_project.repository;

import com.example.final_project.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    boolean existsByQuestionId(Long questionId);
}