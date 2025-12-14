package com.example.final_project.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "exam_online")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamOnline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name; // Tên bài thi

    @Column(name = "num_of_questions", nullable = false)
    private Integer numberOfQuestions; // Số lượng câu hỏi

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamLevel level; // Loại đề thi (khó, dễ, trung bình)

    @Column(name = "submission_deadline", nullable = false)
    private LocalDateTime submissionDeadline; // Thời gian nộp bài

    @Column(name = "passing_score", nullable = false)
    private Integer passingScore; // Điểm đạt

    @Column(name = "max_participants")
    private Integer maxParticipants; // Số người tối đa

    @Column(name = "access_code", unique = true, length = 6)
    private String accessCode; // Mã tham dự (chuỗi 6 số)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamStatus status; // Trạng thái bài thi (PENDING, IN_PROGRESS, FINISHED)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = true) // Allow null for admin-created exams
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToMany
    @JoinTable(
        name = "exam_online_questions",
        joinColumns = @JoinColumn(name = "exam_online_id"),
        inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<Question> questions = new HashSet<>(); // Danh sách câu hỏi cho bài thi

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
