package com.example.final_project.repository;

import com.example.final_project.entity.ExamHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamHistoryRepository extends JpaRepository<ExamHistory, Long> {
}
