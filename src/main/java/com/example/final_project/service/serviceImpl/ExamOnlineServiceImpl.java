package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.ExamOnlineRequest;
import com.example.final_project.dto.ExamOnlineResponse;
import com.example.final_project.dto.ExamOnlineResultsDto;
import com.example.final_project.entity.*;
import com.example.final_project.repository.CategoryRepository;
import com.example.final_project.repository.ExamHistoryRepository;
import com.example.final_project.repository.ExamOnlineRepository;
import com.example.final_project.repository.QuestionRepository;
import com.example.final_project.repository.TeacherRepository;
import com.example.final_project.service.CustomUserDetails;
import com.example.final_project.service.ExamOnlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamOnlineServiceImpl implements ExamOnlineService {

    private final ExamOnlineRepository examOnlineRepository;
    private final TeacherRepository teacherRepository;
    private final QuestionRepository questionRepository;
    private final ExamHistoryRepository examHistoryRepository;
    private final CategoryRepository categoryRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public ExamOnlineResponse createExamOnline(ExamOnlineRequest request, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Teacher teacher = null; // Default to null

        if (isAdmin) {
            // For ADMIN, check for duplicate name among other admin-created exams
            examOnlineRepository.findByNameAndTeacherIsNull(request.getName().trim())
                .ifPresent(e -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An exam with this name already exists for ADMIN.");
                });
        } else {
            // For TEACHER, find their record and check for duplicates under their name
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long teacherId = userDetails.getId();
            teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giáo viên"));
            
            examOnlineRepository.findByNameAndTeacher_TeacherId(request.getName().trim(), teacherId)
                .ifPresent(e -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên bài thi đã tồn tại trong danh sách của bạn");
                });
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));

        List<Question> availableQuestions = questionRepository.findByDifficulty(request.getLevel().name());
        if (availableQuestions.size() < request.getNumberOfQuestions()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không đủ câu hỏi trong ngân hàng cho mức độ và số lượng đã chọn.");
        }
        Set<Question> selectedQuestions = new HashSet<>();
        while (selectedQuestions.size() < request.getNumberOfQuestions()) {
            selectedQuestions.add(availableQuestions.get(secureRandom.nextInt(availableQuestions.size())));
        }

        String code = generateUniqueAccessCode();

        ExamOnline exam = ExamOnline.builder()
                .name(request.getName().trim())
                .numberOfQuestions(request.getNumberOfQuestions())
                .level(request.getLevel())
                .submissionDeadline(request.getSubmissionDeadline())
                .passingScore(request.getPassingScore())
                .maxParticipants(request.getMaxParticipants())
                .accessCode(code)
                .status(ExamStatus.PENDING)
                .teacher(teacher) // Can be null for ADMIN
                .questions(selectedQuestions)
                .category(category)
                .build();

        exam = examOnlineRepository.save(exam);
        return mapToResponse(exam);
    }

    @Override
    @Transactional
    public ExamOnlineResponse startExamOnline(Long examOnlineId, Authentication authentication) {
        ExamOnline exam = getOwnedExam(examOnlineId, authentication);
        if (exam.getStatus() != ExamStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bài thi đã bắt đầu hoặc đã kết thúc, không thể start.");
        }
        exam.setStatus(ExamStatus.IN_PROGRESS);
        return mapToResponse(examOnlineRepository.save(exam));
    }

    @Override
    public ExamOnlineResultsDto getExamOnlineResults(Long examOnlineId, Authentication authentication) {
        ExamOnline exam = getOwnedExam(examOnlineId, authentication);

        List<ExamHistory> histories = examHistoryRepository.findByExamOnline_IdOrderByScoreDesc(examOnlineId);
        long totalParticipants = histories.stream().map(ExamHistory::getStudent).distinct().count();

        List<ExamOnlineResultsDto.StudentResult> detailedResults = histories.stream()
                .map(history -> ExamOnlineResultsDto.StudentResult.builder()
                        .studentId(history.getStudent().getStudentId())
                        .avatarUrl(history.getStudent().getAvatar())
                        .displayName(history.getDisplayName())
                        .score(history.getScore())
                        .correctCount(history.getCorrectCount())
                        .totalQuestions(history.getTotalQuestions())
                        .attemptNumber(history.getAttemptNumber())
                        .build())
                .collect(Collectors.toList());

        return ExamOnlineResultsDto.builder()
                .examName(exam.getName())
                .numberOfParticipants(totalParticipants)
                .numberOfSubmissions(histories.size())
                .results(detailedResults)
                .build();
    }

    @Override
    public List<ExamOnlineResponse> getMyOnlineExams(Long teacherId) {
        return examOnlineRepository.findByTeacher_TeacherIdOrderByCreatedAtDesc(teacherId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ExamOnlineResponse getExamOnlineById(Long examOnlineId, Authentication authentication) {
        return mapToResponse(getOwnedExam(examOnlineId, authentication));
    }

    @Override
    @Transactional
    public ExamOnlineResponse updateExamOnline(Long id, ExamOnlineRequest request, Authentication authentication) {
        ExamOnline exam = getOwnedExam(id, authentication);
        if (exam.getStatus() != ExamStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ được chỉnh sửa bài thi khi trạng thái là PENDING");
        }
        
        if (exam.getTeacher() == null) { // Admin-created exam
             examOnlineRepository.findByNameAndTeacherIsNull(request.getName().trim()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên bài thi đã tồn tại cho ADMIN");
                }
            });
        } else { // Teacher-created exam
            examOnlineRepository.findByNameAndTeacher_TeacherId(request.getName().trim(), exam.getTeacher().getTeacherId())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên bài thi đã tồn tại");
                    }
                });
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));
        exam.setCategory(category);

        exam.setName(request.getName().trim());
        exam.setNumberOfQuestions(request.getNumberOfQuestions());
        exam.setLevel(request.getLevel());
        exam.setSubmissionDeadline(request.getSubmissionDeadline());
        exam.setPassingScore(request.getPassingScore());
        exam.setMaxParticipants(request.getMaxParticipants());
        
        List<Question> availableQuestions = questionRepository.findByDifficulty(request.getLevel().name());
        if (availableQuestions.size() < request.getNumberOfQuestions()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không đủ câu hỏi cho yêu cầu mới.");
        }
        Set<Question> selectedQuestions = new HashSet<>();
        while (selectedQuestions.size() < request.getNumberOfQuestions()) {
            selectedQuestions.add(availableQuestions.get(secureRandom.nextInt(availableQuestions.size())));
        }
        exam.setQuestions(selectedQuestions);

        return mapToResponse(examOnlineRepository.save(exam));
    }

    @Override
    @Transactional
    public ExamOnlineResponse finishExamOnline(Long examOnlineId, Authentication authentication) {
        ExamOnline exam = getOwnedExam(examOnlineId, authentication);
        if (exam.getStatus() != ExamStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể kết thúc bài thi khi trạng thái đang IN_PROGRESS");
        }
        exam.setStatus(ExamStatus.FINISHED);
        return mapToResponse(examOnlineRepository.save(exam));
    }

    @Override
    @Transactional
    public void deleteExamOnlineById(Long examOnlineId, Authentication authentication) {
        ExamOnline exam = getOwnedExam(examOnlineId, authentication);
        if (exam.getStatus() == ExamStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể xóa bài thi đang diễn ra");
        }
        if (examHistoryRepository.existsByExamOnlineId(examOnlineId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể xóa bài thi đã có người làm.");
        }
        examOnlineRepository.delete(exam);
    }

    @Override
    public List<ExamOnlineResponse> getAllOnlineExams() {
        return examOnlineRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ExamOnline getOwnedExam(Long examId, Authentication authentication) {
        ExamOnline exam = examOnlineRepository.findById(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài thi"));
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return exam; // Admin can access any exam
        }

        if (exam.getTeacher() == null) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập bài thi này (dành cho admin).");
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if (!exam.getTeacher().getTeacherId().equals(userDetails.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập bài thi này.");
        }
        return exam;
    }

    private String generateUniqueAccessCode() {
        String code;
        int tries = 20;
        do {
            code = String.format("%06d", secureRandom.nextInt(1_000_000));
            if (--tries == 0) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tạo mã tham dự duy nhất");
            }
        } while (examOnlineRepository.findByAccessCode(code).isPresent());
        return code;
    }

    private ExamOnlineResponse mapToResponse(ExamOnline exam) {
        ExamOnlineResponse dto = new ExamOnlineResponse();
        dto.setId(exam.getId());
        dto.setName(exam.getName());
        dto.setNumberOfQuestions(exam.getNumberOfQuestions());
        dto.setLevel(exam.getLevel());
        dto.setSubmissionDeadline(exam.getSubmissionDeadline());
        dto.setPassingScore(exam.getPassingScore());
        dto.setMaxParticipants(exam.getMaxParticipants());
        dto.setAccessCode(exam.getAccessCode());
        dto.setStatus(exam.getStatus());
        dto.setCreatedAt(exam.getCreatedAt());
        
        if (exam.getTeacher() != null) {
            dto.setTeacherName(exam.getTeacher().getUsername());
        } else {
            dto.setTeacherName("ADMIN");
        }
        if (exam.getCategory() != null) {
            dto.setCategoryId(exam.getCategory().getId());
            dto.setCategoryName(exam.getCategory().getName());
        }
        return dto;
    }
}
