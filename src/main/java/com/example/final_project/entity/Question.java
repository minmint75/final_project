package com.example.final_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.example.final_project.entity.QuestionType type; // Giả định QuestionType là một Enum

    @Column(nullable = false)
    private String difficulty; // "Khó", "Trung bình", "Dễ"

    @ManyToOne
    @JoinColumn(name = "category_id")
    private com.example.final_project.entity.Category category;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.example.final_project.entity.Answer> answers = new ArrayList<>();

    @Column(name = "created_by", nullable = false)
    private String createdBy; // Lưu username hoặc email của người tạo (cho mục đích hiển thị)

    // START CẢI TIẾN: Thêm ID người tạo để kiểm tra quyền
    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;
    // END CẢI TIẾN

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}