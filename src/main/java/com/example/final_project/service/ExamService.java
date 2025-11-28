package com.example.final_project.service;

import com.example.final_project.dto.ExamRequestDto;
import com.example.final_project.dto.ExamResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ExamService {
    ExamResponseDto createExam(ExamRequestDto dto, Long teacherId);
    ExamResponseDto updateExam(Long examId, ExamRequestDto dto, Long teacherId);
    List<ExamResponseDto> getExamsByTeacher(Long teacherId);
    Page<ExamResponseDto> getAllExams(Pageable pageable);
    ExamResponseDto getExamById(Long examId, Long teacherId);
    void deleteExamById(Long examId, Long teacherId);
}