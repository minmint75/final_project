package com.example.final_project.service;

import com.example.final_project.dto.ExamRequestDto;
import com.example.final_project.dto.ExamResponseDto;
import com.example.final_project.dto.ExamSearchRequest;
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

    ExamResponseDto getExamForStudent(Long examId);

    Page<ExamResponseDto> searchExams(ExamSearchRequest searchRequest, Pageable pageable, Long studentId, boolean includeAuthorizedPrivate);

    ExamResponseDto addAllowedStudents(Long examId, com.example.final_project.dto.AllowedStudentsRequest request, Long teacherId);

    List<com.example.final_project.dto.StudentResponseDto> getAllowedStudents(Long examId, Long userId);

    ExamResponseDto joinOfflineExamByCode(String code, Long studentId);
}