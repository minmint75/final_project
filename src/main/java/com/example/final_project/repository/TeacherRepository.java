package com.example.final_project.repository;

import com.example.final_project.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long>, JpaSpecificationExecutor<Teacher> {
    Page<Teacher> findByEmail(String email, Pageable pageable);

    Optional<Teacher> findByUsername(String username);

    Optional<Teacher> findByEmail(String email);

    List<Teacher> findByStatus(Teacher.TeacherStatus status);
}
