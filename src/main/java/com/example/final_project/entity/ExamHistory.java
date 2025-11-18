package com.example.final_project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ====== Thông tin bài thi ======
    @ManyToOne(optional = false)
    @JoinColumn(name = "exam_id")
    private Exam exam;                      // Quan hệ tới bảng Exam, dùng exam.getId() khi cần

    @Column(name = "exam_title", nullable = false, length = 255)
    private String examTitle;               // Tên bài thi

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;         // Số lượng câu hỏi

    @Column(name = "difficulty", nullable = false)
    private String difficulty;              // Độ khó (khó, dễ, trung bình)

    // ====== Thông tin học viên ======

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private Student student;                // Quan hệ tới Student có sẵn

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;             // Tên hiển thị học viên

    // ====== Kết quả làm bài ======

    @Column(name = "score", nullable = false)
    private Double score;                   // Điểm đạt

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;           // Số câu đúng

    @Column(name = "wrong_count", nullable = false)
    private Integer wrongCount;             // Số câu sai

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;      // Thời gian nộp bài

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;          // Lượt thi thứ mấy của học viên với bài thi này

    @PrePersist
    public void prePersist() {
        if (this.submittedAt == null) {
            this.submittedAt = LocalDateTime.now();
        }
        if (this.attemptNumber == null) {
            this.attemptNumber = 1;
        }
    }
}
