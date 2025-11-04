package com.example.final_project.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.final_project.entity.Teacher;

import java.util.List;
import java.util.Optional;

@Repository
    public interface TeacherRepository extends JpaRepository<Teacher, Long>{
    Optional<Teacher> findByUsername(String username);
    Page<Teacher> findByUsername(String username, Pageable pageable);
    Optional<Teacher> findByEmail(String email, Pageable pageable);
    Page<Teacher> findByEmail(String email);

    @Query("SELECT t FROM Teacher t WHERE " +
            "LOWER(t.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Teacher> searchTeachers(@Param("keyword") String keyword, Pageable pageable);

}
