package com.example.final_project.repository;

import com.example.final_project.entity.Question;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Page<Question> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Question> findByCreatedByOrderByCreatedAtDesc(String createdBy, Pageable pageable);
    List<Question> findByDifficulty(String difficulty);
    
    @Query("SELECT q FROM Question q WHERE " +
           "(:keyword IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(q.correctAnswer) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
           "(:type IS NULL OR q.type = :type) AND " +
           "(:categoryId IS NULL OR q.category.id = :categoryId) AND " +
           "(:createdBy IS NULL OR q.createdBy = :createdBy)")
    Page<Question> searchQuestions(@Param("keyword") String keyword,
                                 @Param("difficulty") String difficulty,
                                 @Param("type") String type,
                                 @Param("categoryId") Long categoryId,
                                 @Param("createdBy") String createdBy,
                                 Pageable pageable);
}