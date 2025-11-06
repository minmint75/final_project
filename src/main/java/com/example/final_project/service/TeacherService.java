package com.example.final_project.service;

import com.example.final_project.dto.TeacherSearchRequest;
import com.example.final_project.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TeacherService {

    List<Teacher> findAll();

    Page<Teacher> searchTeachers(TeacherSearchRequest request);

    Page<Teacher> findByEmail(String email, Pageable pageable);

    Optional<Teacher> findById(Long id);
    Teacher save(Teacher teacher);
    void deleteById(Long id);
    Optional<Teacher> findByUsername(String username);
    Optional<Teacher> findByEmail(String email);

    List<Teacher> getPendingTeachers();
    void approveTeacher(Long teacherId);
    void rejectTeacher(Long teacherId);
}