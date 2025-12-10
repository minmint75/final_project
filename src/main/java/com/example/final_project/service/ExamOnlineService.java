package com.example.final_project.service;

import com.example.final_project.dto.*;

import com.example.final_project.entity.ExamOnline;

import org.springframework.security.core.Authentication;



import java.util.List;



public interface ExamOnlineService {



    ExamOnlineResponse createExamOnline(ExamOnlineRequest request, Authentication authentication);



    ExamOnlineResponse startExamOnline(Long examOnlineId, Authentication authentication);



    ExamOnlineResultsDto getExamOnlineResults(Long examOnlineId, Authentication authentication);



    List<ExamOnlineResponse> getMyOnlineExams(Long teacherId); // Keep as is, logic is in controller



    ExamOnlineResponse getExamOnlineById(Long examOnlineId, Authentication authentication);



    ExamOnlineResponse updateExamOnline(Long id, ExamOnlineRequest request, Authentication authentication);



    ExamOnlineResponse finishExamOnline(Long examOnlineId, Authentication authentication);



    void deleteExamOnlineById(Long examOnlineId, Authentication authentication);



    List<ExamOnlineResponse> getAllOnlineExams();



    ExamOnlineJoinResponse joinExamOnline(String accessCode, Authentication authentication);



    ExamOnline findByAccessCode(String accessCode);



    String getWaitingRoomUrl(String accessCode);



    ExamTakeResponseDto getTakeExamOnline(String accessCode, Authentication principal);



    ExamResultResponseDto submitOnlineExam(ExamSubmissionOnlineDto submissionDto, Authentication principal);

}