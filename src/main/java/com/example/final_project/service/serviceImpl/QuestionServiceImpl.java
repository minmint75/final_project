package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.*;
import com.example.final_project.entity.*;
import com.example.final_project.mapper.EntityDtoMapper;
import com.example.final_project.repository.*;
import com.example.final_project.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import com.example.final_project.entity.Difficulty;

@Service
@Transactional
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepo;
    private final CategoryRepository categoryRepo;
    private final ExamQuestionRepository examQuestionRepo;
    private final EntityDtoMapper entityDtoMapper;

    // CREATE
    @Override
    public QuestionResponseDto createQuestion(QuestionCreateDto dto) {
        QuestionType type = QuestionType.valueOf(dto.getType());
        if (type != QuestionType.TRUE_FALSE) {
            validateAnswersByType(type, dto.getAnswers());
        }

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));

        Question q = new Question();
        q.setTitle(dto.getTitle());
        q.setType(type);
        q.setDifficulty(dto.getDifficulty());
        q.setCategory(category);
        q.setCreatedBy(dto.getCreatedBy());

        if (type == QuestionType.TRUE_FALSE) {
            q.setCorrectAnswer(dto.getCorrectAnswer());
            List<Answer> answers = new ArrayList<>();
            Answer trueAnswer = new Answer();
            trueAnswer.setText("True");
            trueAnswer.setCorrect("True".equalsIgnoreCase(dto.getCorrectAnswer()));
            trueAnswer.setQuestion(q);
            answers.add(trueAnswer);

            Answer falseAnswer = new Answer();
            falseAnswer.setText("False");
            falseAnswer.setCorrect("False".equalsIgnoreCase(dto.getCorrectAnswer()));
            falseAnswer.setQuestion(q);
            answers.add(falseAnswer);
            q.setAnswers(answers);
        } else {
            List<Answer> answers = dto.getAnswers().stream().map(aDto -> {
                Answer a = new Answer();
                a.setText(aDto.getText());
                a.setCorrect(Boolean.TRUE.equals(aDto.getCorrect()));
                a.setQuestion(q);
                return a;
            }).collect(Collectors.toList());
            q.setAnswers(answers);
        }

        Question savedQuestion = questionRepo.save(q);
        return entityDtoMapper.toQuestionResponseDto(savedQuestion);
    }

    @Override
    public Page<QuestionResponseDto> searchAndFilterQuestions(
            String keyword,
            String difficulty,
            String type,
            Long categoryId,
            String createdBy,
            Pageable pageable) {
        return QuestionRepository.searchAndFilter(
                keyword,
                difficulty,
                type,
                categoryId,
                createdBy,
                pageable
        );
    }

    // GET SINGLE
    @Override
    public QuestionResponseDto getQuestionById(Long id) {
        Question question = questionRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Câu hỏi không tồn tại"));
        return entityDtoMapper.toQuestionResponseDto(question);
    }

    // LIST (paged, newest first)
    @Override
    public Page<QuestionResponseDto> getAllQuestions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Question> questionPage = questionRepo.findAllByOrderByCreatedAtDesc(pageable);
        return questionPage.map(entityDtoMapper::toQuestionResponseDto);
    }

    // LIST BY USER (paged, newest first)
    @Override
    public Page<QuestionResponseDto> getQuestionsByUser(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Question> questionPage = questionRepo.findByCreatedByOrderByCreatedAtDesc(username, pageable);
        return questionPage.map(entityDtoMapper::toQuestionResponseDto);
    }

    // UPDATE
    @Override
    public QuestionResponseDto updateQuestion(Long id, QuestionUpdateDto dto, String actorUsername) {
        Question q = questionRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Câu hỏi không tồn tại"));

        // Check if already used in exam -> block update
        if (examQuestionRepo.existsByQuestionId(id)) {
            throw new IllegalStateException("Không thể cập nhật câu hỏi đã được chọn vào bài thi.");
        }

        // Check if user is admin or owner
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Allow if user is admin or is the owner
        if (!isAdmin && !q.getCreatedBy().equals(actorUsername)) {
            throw new SecurityException("Không có quyền cập nhật câu hỏi này.");
        }

        QuestionType type = QuestionType.valueOf(dto.getType());
        if (type != QuestionType.TRUE_FALSE) {
            validateAnswersByType(type, dto.getAnswers());
        }

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));

        q.setTitle(dto.getTitle());
        q.setType(type);
        q.setDifficulty(dto.getDifficulty());
        q.setCategory(category);

        // Replace answers
        q.getAnswers().clear();
        if (type == QuestionType.TRUE_FALSE) {
            q.setCorrectAnswer(dto.getCorrectAnswer());
            Answer trueAnswer = new Answer();
            trueAnswer.setText("True");
            trueAnswer.setCorrect("True".equalsIgnoreCase(dto.getCorrectAnswer()));
            trueAnswer.setQuestion(q);
            q.getAnswers().add(trueAnswer);

            Answer falseAnswer = new Answer();
            falseAnswer.setText("False");
            falseAnswer.setCorrect("False".equalsIgnoreCase(dto.getCorrectAnswer()));
            falseAnswer.setQuestion(q);
            q.getAnswers().add(falseAnswer);
        } else {
            q.setCorrectAnswer(null); // Reset correct answer if not TRUE_FALSE
            List<Answer> newAnswers = dto.getAnswers().stream().map(aDto -> {
                Answer a = new Answer();
                a.setText(aDto.getText());
                a.setCorrect(Boolean.TRUE.equals(aDto.getCorrect()));
                a.setQuestion(q);
                return a;
            }).toList();
            q.getAnswers().addAll(newAnswers);
        }

        Question updatedQuestion = questionRepo.save(q);
        return entityDtoMapper.toQuestionResponseDto(updatedQuestion);
    }

    // DELETE
    @Override
    public void deleteQuestion(Long id, String actorUsername) {
        Question q = questionRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Câu hỏi không tồn tại"));;

        if (!q.getCreatedBy().equals(actorUsername)) {
            throw new SecurityException("Không có quyền xóa câu hỏi này.");
        }
        if (examQuestionRepo.existsByQuestionId(id)) {
            throw new IllegalStateException("Không thể xóa câu hỏi đã được chọn vào bài thi.");
        }
        questionRepo.delete(q);
    }

    // ADMIN METHODS - Full permissions
    @Override
    public QuestionResponseDto updateQuestionAsAdmin(Long id, QuestionUpdateDto dto) {
        Question q = questionRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Câu hỏi không tồn tại"));

        // Check if already used in exam -> block update
        if (examQuestionRepo.existsByQuestionId(id)) {
            throw new IllegalStateException("Không thể cập nhật câu hỏi đã được chọn vào bài thi.");
        }

        // Admin can update any question - no ownership check

        QuestionType type = QuestionType.valueOf(dto.getType());
        if (type != QuestionType.TRUE_FALSE) {
            validateAnswersByType(type, dto.getAnswers());
        }

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));

        q.setTitle(dto.getTitle());
        q.setType(type);
        q.setDifficulty(dto.getDifficulty());
        q.setCategory(category);

        // Replace answers
        q.getAnswers().clear();
        if (type == QuestionType.TRUE_FALSE) {
            q.setCorrectAnswer(dto.getCorrectAnswer());
            Answer trueAnswer = new Answer();
            trueAnswer.setText("True");
            trueAnswer.setCorrect("True".equalsIgnoreCase(dto.getCorrectAnswer()));
            trueAnswer.setQuestion(q);
            q.getAnswers().add(trueAnswer);

            Answer falseAnswer = new Answer();
            falseAnswer.setText("False");
            falseAnswer.setCorrect("False".equalsIgnoreCase(dto.getCorrectAnswer()));
            falseAnswer.setQuestion(q);
            q.getAnswers().add(falseAnswer);
        } else {
            q.setCorrectAnswer(null); // Reset correct answer if not TRUE_FALSE
            List<Answer> newAnswers = dto.getAnswers().stream().map(aDto -> {
                Answer a = new Answer();
                a.setText(aDto.getText());
                a.setCorrect(Boolean.TRUE.equals(aDto.getCorrect()));
                a.setQuestion(q);
                return a;
            }).collect(Collectors.toList());
            q.getAnswers().addAll(newAnswers);
        }

        Question updatedQuestion = questionRepo.save(q);
        return entityDtoMapper.toQuestionResponseDto(updatedQuestion);
    }

    @Override
    public void deleteQuestionAsAdmin(Long id) {
        Question q = questionRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Câu hỏi không tồn tại"));

        if (examQuestionRepo.existsByQuestionId(id)) {
            throw new IllegalStateException("Không thể xóa câu hỏi đã được chọn vào bài thi.");
        }

        // Admin can delete any question - no ownership check
        questionRepo.delete(q);
    }

    // Validator function
    private void validateAnswersByType(QuestionType type, List<AnswerDto> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("Phải cung cấp ít nhất một đáp án.");
        }
        long correctCount = answers.stream().filter(a -> Boolean.TRUE.equals(a.getCorrect())).count();

        switch (type) {
            case SINGLE:
                if (correctCount != 1) throw new IllegalArgumentException("Loại " + type + " phải có đúng 1 đáp án đúng.");
                break;
            case MULTIPLE:
                if (correctCount < 2) throw new IllegalArgumentException("Loại MULTIPLE phải có ít nhất 2 đáp án đúng.");
                break;
        }
    }
    private Specification<Question> buildSpecification(
            String keyword,
            String difficulty,
            String type,
            Long categoryId)
    {
        Specification<Question> spec = null; // Bắt đầu với điều kiện trống

        // Lọc theo Từ khóa (Title)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
            spec = ((root, query, cb) -> cb.like(cb.lower(root.get("title")), likeKeyword));
        }

        // Lọc theo Độ khó
        if (difficulty != null && !difficulty.trim().isEmpty()) {
            try {
                Difficulty diffEnum = Difficulty.valueOf(difficulty.toUpperCase());
                Specification<Question> diffSpec = (root, query, cb) ->
                        cb.equal(root.get("difficulty"), diffEnum);

                if (spec == null) {
                    spec = diffSpec;
                } else {
                    spec = spec.and(diffSpec);
                }
            } catch (IllegalArgumentException e) {}
        }
        // Lọc theo Loại câu hỏi (Type)
        if (type != null && !type.trim().isEmpty()) {
            try {
                QuestionType typeEnum = QuestionType.valueOf(type.toUpperCase());

                Specification<Question> typeSpec = (root, query, cb) ->
                        cb.equal(root.get("type"), typeEnum);

                if (spec == null) {
                    spec = typeSpec;
                } else {
                    spec = spec.and(typeSpec);
                }
            } catch (IllegalArgumentException e) {}
        }

        // Lọc theo Danh mục (Category ID)
        if (categoryId != null) {
            Specification<Question> categorySpec = (root, query, cb) ->
                    cb.equal(root.get("category").get("id"), categoryId);

            if (spec == null) {
                spec = categorySpec;
            } else {
                spec = spec.and(categorySpec);
            }
        }
        return spec;
    }
}