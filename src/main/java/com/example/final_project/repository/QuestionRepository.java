package com.example.final_project.repository;

import com.example.final_project.entity.Question;
import com.example.final_project.entity.QuestionType; // Cần import QuestionType
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Phương thức tìm kiếm cơ bản (hiện tại chỉ dùng sắp xếp)
    Page<Question> findAllByOrderByCreatedAtDesc(Pageable pageable);
    @Query("SELECT q FROM Question q JOIN q.answers a " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(q.title) LIKE %:keyword% OR " +
            "LOWER(a.text) LIKE %:keyword%) " +
            "AND (:type IS NULL OR q.type = :type) " +
            "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
            "AND (:categoryId IS NULL OR q.category.id = :categoryId) " +
            "GROUP BY q.id ORDER BY q.createdAt DESC")
    Page<Question> searchQuestions(
            @Param("keyword") String keyword,
            @Param("type") QuestionType type,
            @Param("difficulty") String difficulty,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // Lưu ý: Bạn cần phải định nghĩa Enum QuestionType trong package entity.
}