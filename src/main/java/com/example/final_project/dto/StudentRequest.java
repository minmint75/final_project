package com.example.final_project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.example.final_project.entity.Student;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentRequest {

    private Long studentId;
    private String username;
    private String email;
    private String password;
    private LocalDateTime lastVisit;
    private Student.StudentStatus status;

    public static StudentRequest fromEntity(Student student) {
        if (student == null) {
            return null;
        }
        return new StudentRequest(
                student.getStudentId(),
                student.getUsername(),
                student.getEmail(),
                student.getPassword(),
                student.getLastVisit(),
                student.getStatus()
        );
    }

    public Student toEntity() {
        Student student = new Student();
        student.setStudentId(this.studentId);
        student.setUsername(this.username);
        student.setEmail(this.email);
        student.setPassword(this.password);
        student.setLastVisit(this.lastVisit);
        student.setStatus(this.status);
        return student;
    }
}

