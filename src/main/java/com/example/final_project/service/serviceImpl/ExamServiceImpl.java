package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.ExamRequestDto;
import com.example.final_project.dto.ExamResponseDto;
import com.example.final_project.dto.ExamSearchRequest;
import com.example.final_project.entity.*;
import com.example.final_project.mapper.EntityDtoMapper;
import com.example.final_project.repository.*;
import com.example.final_project.service.ExamService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final TeacherRepository teacherRepository;
    private final QuestionRepository questionRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final CategoryRepository categoryRepository;
    private final EntityDtoMapper entityDtoMapper;

    @Override
    @Transactional
    public ExamResponseDto createExam(ExamRequestDto dto, Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        Teacher teacher = null;
        if (!isAdmin) {
            teacher = teacherRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Không tìm thấy giáo viên với ID: " + userId));
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy danh mục với ID: " + dto.getCategoryId()));

        Exam exam = Exam.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .durationMinutes(dto.getDurationMinutes())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .teacher(teacher)
                .category(category)
                .examLevel(dto.getExamLevel())
                .build();

        exam = examRepository.save(exam);

        // Thêm câu hỏi vào exam
        Exam finalExam = exam;
        AtomicInteger index = new AtomicInteger(0);
        List<ExamQuestion> examQuestions = dto.getQuestionIds().stream().map(qid -> {
            Question question = questionRepository.findById(qid)
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Câu hỏi không tồn tại: " + qid));
            return ExamQuestion.builder()
                    .exam(finalExam)
                    .question(question)
                    .orderIndex(index.getAndIncrement())
                    .build();
        }).collect(Collectors.toList());

        examQuestionRepository.saveAll(examQuestions);
        exam.setExamQuestions(examQuestions);

        return entityDtoMapper.toExamResponseDto(exam);
    }

    @Override
    @Transactional
    public ExamResponseDto updateExam(Long examId, ExamRequestDto dto, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài thi không tồn tại"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (!isAdmin) {
            if (exam.getTeacher() == null || !exam.getTeacher().getTeacherId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền chỉnh sửa bài thi này");
            }
        }

        if (examRepository.hasSubmissions(examId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể cập nhật bài thi đã có người làm");
        }
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));

        exam.setTitle(dto.getTitle());
        exam.setDescription(dto.getDescription());
        exam.setDurationMinutes(dto.getDurationMinutes());
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());
        exam.setCategory(category);
        exam.setExamLevel(dto.getExamLevel());

        if (exam.getExamQuestions() == null) {
            exam.setExamQuestions(new ArrayList<>());
        } else {
            exam.getExamQuestions().clear();
        }
        AtomicInteger index = new AtomicInteger(0);
        dto.getQuestionIds().forEach(qid -> {
            Question question = questionRepository.findById(qid)
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Câu hỏi không tồn tại: " + qid));
            ExamQuestion examQuestion = ExamQuestion.builder()
                    .exam(exam)
                    .question(question)
                    .orderIndex(index.getAndIncrement())
                    .build();
            exam.getExamQuestions().add(examQuestion);
        });

        Exam savedExam = examRepository.save(exam);
        return entityDtoMapper.toExamResponseDto(savedExam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamResponseDto> getExamsByTeacher(Long teacherId) {
        return examRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId).stream()
                .map(entityDtoMapper::toExamResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExamResponseDto> getAllExams(Pageable pageable) {
        Page<Exam> examPage = examRepository.findAll(pageable);
        return examPage.map(entityDtoMapper::toExamResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ExamResponseDto getExamById(Long examId, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài thi không tồn tại"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (!isAdmin) {
            if (exam.getTeacher() == null || !exam.getTeacher().getTeacherId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem bài thi này");
            }
        }
        return entityDtoMapper.toExamResponseDto(exam);
    }

    @Override
    public void deleteExamById(Long examId, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài thi không tồn tại"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (!isAdmin) {
            if (exam.getTeacher() == null || !exam.getTeacher().getTeacherId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xóa bài thi này");
            }
        }
        if (examRepository.hasSubmissions(examId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể xóa bài thi đã có người làm");
        }
        examRepository.deleteById(examId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExamResponseDto> searchExams(ExamSearchRequest searchRequest, Pageable pageable) {
        Specification<Exam> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchRequest.getCategoryId() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("category").get("categoryId"), searchRequest.getCategoryId()));
            }

            if (StringUtils.hasText(searchRequest.getTitle())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                        "%" + searchRequest.getTitle().toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return examRepository.findAll(spec, pageable).map(entityDtoMapper::toExamResponseDto);
    }
}