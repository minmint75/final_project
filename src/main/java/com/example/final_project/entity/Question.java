package com.example.final_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionVisibility visibility = QuestionVisibility.PRIVATE;

    @Column(nullable = false)
    private String difficulty;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    @Column(nullable = true)
    private String correctAnswer;

    @Column(name = "created_by", nullable = false)
    private String createdBy; // có thể lưu username hoặc id dưới dạng string

    @Column(name = "created_by_name", nullable = true)
    private String createdByName;

    @Column(name = "created_by_role", nullable = true)
    private String createdByRole;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}