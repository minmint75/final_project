package com.example.final_project.repository;

import com.example.final_project.entity.Question;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Page<Question> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Question> findByCreatedByOrderByCreatedAtDesc(String createdBy, Pageable pageable);
    List<Question> findByDifficulty(String difficulty);
}