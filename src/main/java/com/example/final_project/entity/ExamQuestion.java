package com.example.final_project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_question")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_question_id")
    private Long examQuestionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "order_index")
    private Integer orderIndex;
}