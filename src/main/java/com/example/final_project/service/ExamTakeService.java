package com.example.final_project.service;

import com.example.final_project.dto.ExamSubmissionRequestDto;
import com.example.final_project.dto.ExamResultResponseDto;
import com.example.final_project.dto.ExamTakeResponseDto;

public interface ExamTakeService {
    ExamTakeResponseDto startExam(Long examId, Long studentId);
    ExamResultResponseDto submitExam(ExamSubmissionRequestDto submissionDto, Long studentId);
}
