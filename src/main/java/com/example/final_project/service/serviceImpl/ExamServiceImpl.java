package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.ExamRequestDto;
import com.example.final_project.dto.ExamResponseDto;
import com.example.final_project.entity.*;
import com.example.final_project.mapper.EntityDtoMapper;
import com.example.final_project.repository.*;
import com.example.final_project.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ExamResponseDto createExam(ExamRequestDto dto, Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên"));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        Exam exam = Exam.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .durationMinutes(dto.getDurationMinutes())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .teacher(teacher)
                .category(category)
                .build();

        exam = examRepository.save(exam);

        // Thêm câu hỏi vào exam
        Exam finalExam = exam;
        AtomicInteger index = new AtomicInteger(0);
        List<ExamQuestion> examQuestions = dto.getQuestionIds().stream().map(qid -> {
            Question question = questionRepository.findById(qid)
                    .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại: " + qid));
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
    public ExamResponseDto updateExam(Long examId, ExamRequestDto dto, Long teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài thi không tồn tại"));

        if (!exam.getTeacher().getTeacherId().equals(teacherId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bài thi này");
        }

        if (examRepository.hasSubmissions(examId)) {
            throw new RuntimeException("Không thể cập nhật bài thi đã có người làm");
        }
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));


        exam.setTitle(dto.getTitle());
        exam.setDescription(dto.getDescription());
        exam.setDurationMinutes(dto.getDurationMinutes());
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());
        exam.setCategory(category);

        // Xóa câu hỏi cũ
        examQuestionRepository.deleteAll(exam.getExamQuestions());
        exam.getExamQuestions().clear();

        // Thêm câu hỏi mới
        AtomicInteger index = new AtomicInteger(0);
        List<ExamQuestion> newQuestions = dto.getQuestionIds().stream().map(qid -> {
            Question q = questionRepository.findById(qid)
                    .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại: " + qid));
            return ExamQuestion.builder()
                    .exam(exam)
                    .question(q)
                    .orderIndex(index.getAndIncrement())
                    .build();
        }).collect(Collectors.toList());

        examQuestionRepository.saveAll(newQuestions);
        exam.setExamQuestions(newQuestions);

        Exam savedExam = examRepository.save(exam);
        return entityDtoMapper.toExamResponseDto(savedExam);
    }

    @Override
    public List<ExamResponseDto> getExamsByTeacher(Long teacherId) {
        return examRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId).stream()
                .map(entityDtoMapper::toExamResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ExamResponseDto> getAllExams(Pageable pageable) {
        Page<Exam> examPage = examRepository.findAll(pageable);
        return examPage.map(entityDtoMapper::toExamResponseDto);
    }

    @Override
    public ExamResponseDto getExamById(Long examId, Long teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài thi không tồn tại"));
        if (!exam.getTeacher().getTeacherId().equals(teacherId)) {
            throw new RuntimeException("Bạn không có quyền xem bài thi này");
        }
        return entityDtoMapper.toExamResponseDto(exam);
    }

    @Override
    public void deleteExamById(Long examId, Long teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài thi không tồn tại"));
        if (!exam.getTeacher().getTeacherId().equals(teacherId)) {
            throw new RuntimeException("Bạn không có quyền xóa bài thi này");
        }
        if (examRepository.hasSubmissions(examId)) {
            throw new RuntimeException("Không thể xóa bài thi đã có người làm");
        }
        examRepository.deleteById(examId);
    }
}