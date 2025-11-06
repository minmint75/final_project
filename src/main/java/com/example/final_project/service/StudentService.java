package com.example.final_project.service;

import com.example.final_project.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

import java.util.List;

public interface StudentService {

    List<Student> findAll();

    Page<Student> searchStudent(String keyword, Pageable pageable);

    Page<Student> findByEmail(String email, Pageable pageable);

    Optional<Student> findById(Long id);
    Student save(Student student);
    void deleteById(Long id);
    Optional<Student> findByUsername(String username);
    Optional<Student> findByEmail(String email);
}