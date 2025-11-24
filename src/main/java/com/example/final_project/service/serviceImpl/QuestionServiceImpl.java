package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.AnswerDto;
import com.example.final_project.dto.QuestionCreateDto;
import com.example.final_project.dto.QuestionUpdateDto;
import com.example.final_project.entity.Answer;
import com.example.final_project.entity.Category;
import com.example.final_project.entity.Question;
import com.example.final_project.entity.QuestionType; // Giả định Enum QuestionType
import com.example.final_project.repository.CategoryRepository;
import com.example.final_project.repository.ExamQuestionRepository;
import com.example.final_project.repository.QuestionRepository;
import com.example.final_project.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional // ĐÃ SỬA: Loại bỏ abstract
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionRepository QuestionRepo;
    @Autowired
    private CategoryRepository CategoryRepo;
    @Autowired
    private ExamQuestionRepository ExamQuestionRepo;

    // THAY THẾ BẰNG LOGIC AUTH THỰC TẾ: Giả định lấy User ID và Role từ Username/Context
    private static final Map<String, Long> USER_MAP = Map.of("admin", 1L, "teacher_a", 101L, "teacher_b", 102L);
    private String getRoleFromUsername(String username) {
        if ("admin".equalsIgnoreCase(username)) return "ADMIN";
        return "TEACHER";
    }
    private Long getUserIdFromUsername(String username) {
        // Trong môi trường thực tế, bạn sẽ tra cứu User ID từ database hoặc token
        return USER_MAP.getOrDefault(username.toLowerCase(), 0L);
    }

    // --- CREATE ---
    @Override
    public Question createQuestion(QuestionCreateDto dto) {
        QuestionType type = QuestionType.valueOf(dto.getType());
        validateAnswersByType(type, dto.getAnswers());

        Category category = CategoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));

        Question q = new Question();
        q.setTitle(dto.getTitle());
        q.setType(type);
        q.setDifficulty(dto.getDifficulty());
        q.setCategory(category);
        q.setCreatedBy(dto.getCreatedBy()); // Username
        q.setCreatedByUserId(getUserIdFromUsername(dto.getCreatedBy())); // ĐÃ THÊM: Lưu User ID

        List<Answer> answers = dto.getAnswers().stream().map(aDto -> {
            Answer a = new Answer();
            a.setText(aDto.getText());
            a.setCorrect(Boolean.TRUE.equals(aDto.getCorrect()));
            a.setQuestion(q);
            return a;
        }).collect(Collectors.toList());
        q.setAnswers(answers);

        return QuestionRepo.save(q);
    }

    // --- GET SINGLE ---
    @Override
    public Question getQuestionById(Long id) {
        return QuestionRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Câu hỏi không tồn tại"));
    }

    // --- LIST ALL ---
    @Override
    public Page<Question> getAllQuestions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return QuestionRepo.findAll(pageable);
    }

    // --- SEARCH/FILTER ---
    @Override
    public Page<Question> searchQuestions(int page, int size, String search, String difficulty, String type, String category) {
        Pageable pageable = PageRequest.of(page, size);

        QuestionType questionType = (type != null && !type.isEmpty()) ? QuestionType.valueOf(type.toUpperCase()) : null;
        Long categoryId = (category != null && !category.isEmpty()) ? Long.valueOf(category) : null;
        String keyword = (search != null && !search.isEmpty()) ? search.toLowerCase() : null;
        String diff = (difficulty != null && !difficulty.isEmpty()) ? difficulty : null;

        // Gọi phương thức từ QuestionRepository đã được tạo trước đó
        return QuestionRepo.searchQuestions(
                keyword,
                questionType,
                diff,
                categoryId,
                pageable
        );
    }

    // --- UPDATE ---
    @Override
    public Question updateQuestion(Long id, QuestionUpdateDto dto, String actorUsername) {
        Question q = getQuestionById(id);

        // 1. KIỂM TRA QUYỀN SỬA
        String actorRole = getRoleFromUsername(actorUsername);
        Long actorId = getUserIdFromUsername(actorUsername);

        // SỬA: Chỉ Admin hoặc người tạo mới được sửa
        if (!actorRole.equals("ADMIN") && !q.getCreatedByUserId().equals(actorId)) {
            throw new SecurityException("Không có quyền cập nhật câu hỏi này. (Chỉ Admin hoặc người tạo)");
        }

        // 2. KIỂM TRA ĐANG SỬ DỤNG TRONG BÀI THI
        if (ExamQuestionRepo.existsByQuestionId(id)) {
            throw new IllegalStateException("Không thể cập nhật câu hỏi đã được chọn vào bài thi.");
        }

        // 3. VALIDATE VÀ CẬP NHẬT
        QuestionType type = QuestionType.valueOf(dto.getType());
        validateAnswersByType(type, dto.getAnswers());

        Category category = CategoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));

        q.setTitle(dto.getTitle());
        q.setType(type);
        q.setDifficulty(dto.getDifficulty());
        q.setCategory(category);

        // 4. Thay thế answers
        q.getAnswers().clear();
        List<Answer> newAnswers = dto.getAnswers().stream().map(aDto -> {
            Answer a = new Answer();
            a.setText(aDto.getText());
            a.setCorrect(Boolean.TRUE.equals(aDto.getCorrect()));
            a.setQuestion(q);
            return a;
        }).collect(Collectors.toList());
        q.getAnswers().addAll(newAnswers);

        return QuestionRepo.save(q);
    }

    // --- DELETE ---
    @Override
    public void deleteQuestion(Long id, String actorUsername) {
        Question q = getQuestionById(id);

        // 1. KIỂM TRA QUYỀN XÓA
        String actorRole = getRoleFromUsername(actorUsername);
        Long actorId = getUserIdFromUsername(actorUsername);

        // SỬA: Chỉ Admin hoặc người tạo mới được xóa
        if (!actorRole.equals("ADMIN") && !q.getCreatedByUserId().equals(actorId)) {
            throw new SecurityException("Không có quyền xóa câu hỏi này. (Chỉ Admin hoặc người tạo)");
        }

        // 2. KIỂM TRA ĐANG SỬ DỤNG TRONG BÀI THI
        if (ExamQuestionRepo.existsByQuestionId(id)) {
            throw new IllegalStateException("Không thể xóa câu hỏi đã được chọn vào bài thi.");
        }

        QuestionRepo.delete(q);
    }

    // --- VALIDATOR ---
    private void validateAnswersByType(QuestionType type, List<AnswerDto> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("Phải cung cấp ít nhất một đáp án.");
        }
        long correctCount = answers.stream().filter(a -> Boolean.TRUE.equals(a.getCorrect())).count();

        switch (type) {
            case SINGLE:
            case TRUE_FALSE:
                if (correctCount != 1) throw new IllegalArgumentException("Loại " + type + " phải có đúng 1 đáp án đúng.");
                break;
            case MULTIPLE:
                if (correctCount < 2) throw new IllegalArgumentException("Loại MULTIPLE phải có ít nhất 2 đáp án đúng.");
                break;
            default:
                // Xử lý các loại câu hỏi khác nếu cần
                break;
        }
    }
}