package com.example.final_project.repository;

import com.example.final_project.entity.Question;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Page<Question> findAllByOrderByCreatedAtDesc(Pageable pageable);
}