package com.example.final_project.repository;

import com.example.final_project.entity.ExamOnline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamOnlineRepository extends JpaRepository<ExamOnline, Long> {
    /**
     * Finds an ExamOnline by its name and the ID of the teacher who created it.
     * Used for validating unique exam names per teacher.
     *
     * @param name The name of the online exam.
     * @param teacherId The ID of the teacher.
     * @return An Optional containing the ExamOnline if found, or empty otherwise.
     */
    Optional<ExamOnline> findByNameAndTeacher_TeacherId(String name, Long teacherId);
    Optional<ExamOnline> findByAccessCode(String accessCode);
}
