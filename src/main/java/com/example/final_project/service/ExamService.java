package com.example.final_project.service;

import com.example.final_project.dto.ExamRequestDto;
import com.example.final_project.entity.Exam;

import java.util.List;

public interface ExamService {
    Exam createExam(ExamRequestDto dto, Long teacherId);
    Exam updateExam(Long examId, ExamRequestDto dto, Long teacherId);
    List<Exam> getExamsByTeacher(Long teacherId);
    Exam getExamById(Long examId, Long teacherId);
    void deleteExamById(Long examId, Long teacherId);
}