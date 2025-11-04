package com.example.final_project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.example.final_project.entity.Teacher;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherRequest {

    private Long teacherId;
    private String username;
    private String email;
    private String password;
    private LocalDateTime lastVisit;
    private Teacher.TeacherStatus status;

    public static TeacherRequest fromEntity(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        return new TeacherRequest(
                teacher.getTeacherId(),
                teacher.getUsername(),
                teacher.getPassword(),
                teacher.getEmail(),
                teacher.getLastVisit(),
                teacher.getStatus()
        );
    }

    public Teacher toEntity() {
        Teacher teacher = new Teacher();
        teacher.setTeacherId(this.teacherId);
        teacher.setUsername(this.username);
        teacher.setPassword(this.password);
        teacher.setEmail(this.email);
        teacher.setLastVisit(this.lastVisit);
        teacher.setStatus(this.status);
        return teacher;
    }
}

