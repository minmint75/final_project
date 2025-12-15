package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.AllowedStudentsRequest;
import com.example.final_project.dto.ExamRequestDto;
import com.example.final_project.dto.ExamResponseDto;
import com.example.final_project.dto.ExamSearchRequest;
import com.example.final_project.entity.*;
import com.example.final_project.mapper.EntityDtoMapper;
import com.example.final_project.repository.*;
import com.example.final_project.service.ExamService;
import com.example.final_project.util.CodeGenerator;
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
import java.util.Collections;
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
    private final CodeGenerator codeGenerator;
    private final StudentRepository studentRepository;

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

        if (examRepository.existsByTitle(dto.getTitle())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên bài thi đã tồn tại");
        }

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

        if (exam.getStatus() == ExamStatus.PRIVATE) {
            String code = codeGenerator.generateUniqueCode();
            exam.setCode(code);
            exam.setUrl(codeGenerator.generateUrl(code));
        }

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

        if (exam.getStatus() == ExamStatus.PRIVATE && exam.getCode() == null) {
            String code = codeGenerator.generateUniqueCode();
            exam.setCode(code);
            exam.setUrl(codeGenerator.generateUrl(code));
        }

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
        boolean isStudent = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"));

        if (!isAdmin && !isStudent) {
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
    public ExamResponseDto getExamForStudent(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài thi không tồn tại"));
        return entityDtoMapper.toExamResponseDto(exam);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExamResponseDto> searchExams(ExamSearchRequest searchRequest, Pageable pageable, Long studentId, boolean includeAuthorizedPrivate) {
        Specification<Exam> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchRequest.getCategoryId() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("category").get("id"), searchRequest.getCategoryId()));
            }

            if (StringUtils.hasText(searchRequest.getTitle())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                        "%" + searchRequest.getTitle().toLowerCase() + "%"));
            }

            if (searchRequest.getExamLevel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("examLevel"), searchRequest.getExamLevel()));
            }

            if (searchRequest.getTeacherId() != null) {
                predicates
                        .add(criteriaBuilder.equal(root.get("teacher").get("teacherId"), searchRequest.getTeacherId()));
            }

            if (studentId != null) { // Logic for student view
                if (includeAuthorizedPrivate) {
                    predicates.add(criteriaBuilder.or(
                            // PUBLISHED exams
                            criteriaBuilder.equal(root.get("status"), ExamStatus.PUBLISHED),
                            // PRIVATE exams where student is authorized
                            criteriaBuilder.and(
                                    criteriaBuilder.equal(root.get("status"), ExamStatus.PRIVATE),
                                    criteriaBuilder.isMember(studentRepository.getReferenceById(studentId), root.get("allowedStudents"))
                            )
                    ));
                } else {
                    // Only PUBLISHED exams if not including authorized private
                    predicates.add(criteriaBuilder.equal(root.get("status"), ExamStatus.PUBLISHED));
                }
            } else { // Logic for Admin/Teacher view or general search
                if (searchRequest.getStatus() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), searchRequest.getStatus()));
                } else {
                    // Default for general search (e.g., show all PUBLISHED for non-admin/teacher if status not specified)
                    // Or let other predicates handle it if no status filter
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

                                            return examRepository.findAll(spec, pageable).map(exam -> {

                                            ExamResponseDto dto = entityDtoMapper.toExamResponseDto(exam);

                                            // Set isAuthorized flag based on student context if available

                                            if (studentId != null) {

                                                if (exam.getStatus() == ExamStatus.PRIVATE) {

                                                    // Explicitly initialize the collection to ensure it's loaded within the transaction

                                                    // Accessing .size() or .isEmpty() is a common way to trigger lazy loading

                                                    if (exam.getAllowedStudents() != null) {

                                                         exam.getAllowedStudents().size(); // Trigger lazy loading

                                                    }

                                

                                                    dto.setAuthorized(exam.getAllowedStudents() != null &&

                                                            exam.getAllowedStudents().stream().anyMatch(s -> s.getStudentId().equals(studentId)));

                                                } else {

                                                    dto.setAuthorized(true); // Public exams are always authorized for students

                                                }

                                            } else {

                                                dto.setAuthorized(true); // Default to true if no student context (e.g., admin/teacher view)

                                            }

                                            return dto;

                                        });    }

    @Override
    @Transactional
    public ExamResponseDto addAllowedStudents(Long examId, AllowedStudentsRequest request, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài thi không tồn tại"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (!isAdmin) {
            if (exam.getTeacher() == null || !exam.getTeacher().getTeacherId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền chỉnh sửa bài thi này");
            }
        }

        if (exam.getStatus() != ExamStatus.PRIVATE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể thêm học viên vào bài thi ở trạng thái PRIVATE");
        }

        List<Student> students = studentRepository.findByEmailIn(request.getStudentEmails());
        if (students.size() != request.getStudentEmails().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Một hoặc nhiều email học viên không tồn tại");
        }

        List<Student> allowedStudents = exam.getAllowedStudents();
        if (allowedStudents == null) {
            allowedStudents = new ArrayList<>();
        }
        allowedStudents.addAll(students);
        exam.setAllowedStudents(allowedStudents.stream().distinct().collect(Collectors.toList()));

        Exam savedExam = examRepository.save(exam);
        return entityDtoMapper.toExamResponseDto(savedExam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.example.final_project.dto.StudentResponseDto> getAllowedStudents(Long examId, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài thi không tồn tại"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (!isAdmin) {
            if (exam.getTeacher() == null || !exam.getTeacher().getTeacherId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem danh sách này");
            }
        }

        if (exam.getAllowedStudents() == null) {
            return Collections.emptyList();
        }

        return exam.getAllowedStudents().stream()
                .map(entityDtoMapper::toStudentResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExamResponseDto joinOfflineExamByCode(String code, Long studentId) {
        Exam exam = examRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mã bài thi không hợp lệ"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Học viên không tồn tại"));

        // Only allow joining PUBLISHED or PRIVATE exams
        if (exam.getStatus() != ExamStatus.PUBLISHED && exam.getStatus() != ExamStatus.PRIVATE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bài thi không khả dụng để tham gia");
        }

        // If private, check if student is allowed or add them
        if (exam.getStatus() == ExamStatus.PRIVATE) {
            if (exam.getAllowedStudents() == null) {
                exam.setAllowedStudents(new ArrayList<>());
            }
            if (!exam.getAllowedStudents().contains(student)) {
                exam.getAllowedStudents().add(student);
                examRepository.save(exam); // Save to update allowed students list
            }
        }

        return entityDtoMapper.toExamResponseDto(exam);
    }
}
