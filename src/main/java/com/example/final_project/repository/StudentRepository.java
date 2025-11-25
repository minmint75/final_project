package com.example.final_project.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.final_project.entity.Student;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUsername(String username);
    Optional<Student> findByEmail(String email);
    Page<Student> findByEmail(String email, Pageable pageable);

    @Query("SELECT t FROM Student t WHERE " +
            "LOWER(t.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.email) LIKE LOWER(CONCAT('%', :keyword, '%'))" )
    Page<Student> searchStudents(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT s FROM Student s WHERE LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    Page<Student> searchByEmail(@Param("email") String email, Pageable pageable);
    
    @Query("SELECT s FROM Student s WHERE LOWER(s.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<Student> searchByUsername(@Param("username") String username, Pageable pageable);
    
    @Query("SELECT s FROM Student s WHERE " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%')) AND " +
            "LOWER(s.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<Student> searchByEmailAndUsername(@Param("email") String email, @Param("username") String username, Pageable pageable);
}
