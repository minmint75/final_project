package com.example.final_project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Student", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email", name = "uk_student_email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name")
    private RoleName roleName = RoleName.STUDENT;

    @Column(name = "user_name", nullable = false, length = 100)
    @NotBlank(message = "Tên học viên không được để trống")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    private String avatar;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name ="last_visit")
    private LocalDateTime lastVisit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Status không được để trống")
    private StudentStatus status = StudentStatus.ACTIVE;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = StudentStatus.PENDING;
        }
    }

    public enum StudentStatus {
        PENDING("Đang chờ xác nhận"),
        ACTIVE("Hoạt động"),
        LOCKED("Bị khóa");

        private final String displayName;

        StudentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
