package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.ExamRequestDto;
import com.example.final_project.entity.*;
import com.example.final_project.repository.*;
import com.example.final_project.service.ExamService;
import lombok.RequiredArgsConstructor;
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

    @Override
    @Transactional
    public Exam createExam(ExamRequestDto dto, Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên"));

        // Tìm hoặc tạo category từ description
        Category category = null;
        if (dto.getDescription() != null && !dto.getDescription().trim().isEmpty()) {
            category = categoryRepository.findByName(dto.getDescription())
                    .orElseGet(() -> {
                        Category newCategory = new Category();
                        newCategory.setName(dto.getDescription());
                        newCategory.setCreatedBy(teacher.getEmail());
                        return categoryRepository.save(newCategory);
                    });
        }

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

        return exam;
    }

    @Override
    @Transactional
    public Exam updateExam(Long examId, ExamRequestDto dto, Long teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài thi không tồn tại"));

        if (!exam.getTeacher().getTeacherId().equals(teacherId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bài thi này");
        }

        if (examRepository.hasSubmissions(examId)) {
            throw new RuntimeException("Không thể cập nhật bài thi đã có người làm");
        }

        exam.setTitle(dto.getTitle());
        exam.setDescription(dto.getDescription());
        exam.setDurationMinutes(dto.getDurationMinutes());
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());

        // Xóa câu hỏi cũ
        examQuestionRepository.deleteAll(exam.getExamQuestions());

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

        return examRepository.save(exam);
    }

    @Override
    public List<Exam> getExamsByTeacher(Long teacherId) {
        return examRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public Exam getExamById(Long examId, Long teacherId) {
        // Step 1: Fetch exam với examQuestions và question (không có answers)
        Exam exam = examRepository.findByIdWithQuestions(examId)
                .orElseThrow(() -> new RuntimeException("Bài thi không tồn tại"));
        
        if (!exam.getTeacher().getTeacherId().equals(teacherId)) {
            throw new RuntimeException("Bạn không có quyền xem bài thi này");
        }
        
        // Step 2: Fetch tất cả answers cho các questions
        if (exam.getExamQuestions() != null && !exam.getExamQuestions().isEmpty()) {
            List<Long> questionIds = exam.getExamQuestions().stream()
                    .map(eq -> eq.getQuestion().getId())
                    .toList();
            
            // Load questions với answers
            List<Question> questionsWithAnswers = examRepository.findByIdWithAnswers(questionIds);
            
            // Map answers vào questions đã có trong exam
            exam.getExamQuestions().forEach(eq -> {
                Question questionWithAnswers = questionsWithAnswers.stream()
                        .filter(q -> q.getId().equals(eq.getQuestion().getId()))
                        .findFirst()
                        .orElse(null);
                if (questionWithAnswers != null) {
                    eq.getQuestion().setAnswers(questionWithAnswers.getAnswers());
                }
            });
        }
        
        return exam;
    }

    @Override
    public void deleteExamById(Long examId, Long teacherId) {
    }
}