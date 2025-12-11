package com.example.final_project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
public class ExamHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ====== Thông tin bài thi ======
    @ManyToOne(optional = true) // Made optional to support ExamOnline
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @ManyToOne(optional = true) // Added for ExamOnline
    @JoinColumn(name = "exam_online_id")
    private ExamOnline examOnline;

    @Column(name = "exam_title", nullable = false, length = 255)
    private String examTitle;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "difficulty", nullable = false)
    private String difficulty;

    // ====== Thông tin học viên ======
    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    // ====== Kết quả làm bài ======
    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    @Column(name = "wrong_count", nullable = false)
    private Integer wrongCount = 0;

    @Column(name = "passed")
    private Boolean passed;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @OneToMany(mappedBy = "examHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ExamHistoryDetail> details;

    @PrePersist
    public void prePersist() {
        if (this.submittedAt == null) {
            this.submittedAt = LocalDateTime.now();
        }
        if (this.attemptNumber == null) {
            this.attemptNumber = 1;
        }
        if (this.wrongCount == null) {
            this.wrongCount = 0;
        }
        if (this.passed == null) {
            this.passed = false;
        }
    }
}
