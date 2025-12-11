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

@Service
@Transactional
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepo;
    private final CategoryRepository categoryRepo;
    private final ExamQuestionRepository examQuestionRepo;
    private final EntityDtoMapper entityDtoMapper;
    private final TeacherRepository teacherRepo;
    private final AdminRepository adminRepo;

    // CREATE
    @Override
    public QuestionResponseDto createQuestion(QuestionCreateDto dto) {
        QuestionType type = QuestionType.valueOf(dto.getType());
        validateAnswersByType(type, dto.getAnswers());

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));

        Question q = new Question();
        q.setTitle(dto.getTitle());
        q.setType(type);
        q.setVisibility(QuestionVisibility.valueOf(dto.getVisibility().toUpperCase()));
        q.setDifficulty(dto.getDifficulty());
        q.setCategory(category);
        q.setCreatedBy(dto.getCreatedBy());

        if (type == QuestionType.TRUE_FALSE) {
            String currentCorrectAnswerText = dto.getAnswers().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getCorrect()))
                    .map(AnswerDto::getText)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Đáp án đúng cho TRUE_FALSE phải có một đáp án đúng được chọn.")); // This should not happen if validateAnswersByType passed.

            // Additional check for the text value, as validateAnswersByType no longer does this.
            if (!"True".equalsIgnoreCase(currentCorrectAnswerText) && !"False".equalsIgnoreCase(currentCorrectAnswerText)
                    && !"Đúng".equalsIgnoreCase(currentCorrectAnswerText) && !"Sai".equalsIgnoreCase(currentCorrectAnswerText)) {
                throw new IllegalArgumentException("Đáp án đúng cho TRUE_FALSE phải là 'True', 'False', 'Đúng' hoặc 'Sai'.");
            }

            q.setCorrectAnswer(currentCorrectAnswerText); // e.g., "Đúng"

            List<Answer> answers = new ArrayList<>();
            for (AnswerDto answerDto : dto.getAnswers()) { // Iterate through the DTO's answers
                Answer answer = new Answer();
                answer.setText(answerDto.getText()); // Use the text from DTO, e.g., "Đúng" or "Sai"
                // Set correct based on whether its text matches the currentCorrectAnswerText
                answer.setCorrect(answerDto.getText().equalsIgnoreCase(currentCorrectAnswerText));
                answer.setQuestion(q);
                answers.add(answer);
            }
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

            // New logic: Populate correct_answer for SINGLE/MULTIPLE
            List<String> correctTexts = dto.getAnswers().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getCorrect()))
                    .map(AnswerDto::getText)
                    .collect(Collectors.toList());
            q.setCorrectAnswer(String.join("||", correctTexts));
        }

        Question savedQuestion = questionRepo.save(q);

        // Increment totalQuestions for the associated category
        category.setTotalQuestions(category.getTotalQuestions() + 1);
        categoryRepo.save(category);

        return populateCreatorInfo(entityDtoMapper.toQuestionResponseDto(savedQuestion));
    }

    // GET SINGLE
    @Override
    public QuestionResponseDto getQuestionById(Long id) {
        Question question = questionRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Câu hỏi không tồn tại"));

        if (question.getVisibility() == QuestionVisibility.PRIVATE
                || question.getVisibility() == QuestionVisibility.HIDDEN) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            if (!question.getCreatedBy().equals(currentUsername)) {
                throw new SecurityException("Không có quyền xem câu hỏi này.");
            }
        }

        return populateCreatorInfo(entityDtoMapper.toQuestionResponseDto(question));
    }

    // LIST (paged, newest first)
    @Override
    public Page<QuestionResponseDto> getAllQuestions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Page<Question> questionPage = questionRepo.findAllPublicOrCreator(currentUsername, pageable);
        return questionPage.map(entityDtoMapper::toQuestionResponseDto).map(this::populateCreatorInfo);
    }

    // LIST BY USER (paged, newest first)
    @Override
    public Page<QuestionResponseDto> getQuestionsByUser(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Question> questionPage = questionRepo.findByCreatedByOrderByCreatedAtDesc(username, pageable);
        return questionPage.map(entityDtoMapper::toQuestionResponseDto).map(this::populateCreatorInfo);
    }

    // UPDATE
    @Override
    public QuestionResponseDto updateQuestion(Long id, QuestionUpdateDto dto, String actorUsername) {
        Question q = questionRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Câu hỏi không tồn tại"));

        // Check if user is admin or owner
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Only allow owner to update
        if (!isAdmin && !q.getCreatedBy().equals(actorUsername)) {
            throw new SecurityException("Không có quyền cập nhật câu hỏi này.");
        }

        Category oldCategory = q.getCategory(); // Get the current category of the question

        QuestionType type = QuestionType.valueOf(dto.getType());
        validateAnswersByType(type, dto.getAnswers());

        Category newCategory = categoryRepo.findById(dto.getCategoryId()) // This is the NEW category
                .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));

        q.setTitle(dto.getTitle());
        q.setType(type);
        if (dto.getVisibility() != null && !dto.getVisibility().isBlank()) {
            q.setVisibility(QuestionVisibility.valueOf(dto.getVisibility().toUpperCase()));
        }
        q.setDifficulty(dto.getDifficulty());
        q.setCategory(newCategory); // Set the new category

        // Check if category has changed
        if (!oldCategory.getId().equals(newCategory.getId())) {
            // Decrement old category count
            oldCategory.setTotalQuestions(oldCategory.getTotalQuestions() - 1);
            categoryRepo.save(oldCategory);

            // Increment new category count
            newCategory.setTotalQuestions(newCategory.getTotalQuestions() + 1);
            categoryRepo.save(newCategory);
        }

        // Replace answers
        q.getAnswers().clear();
        if (type == QuestionType.TRUE_FALSE) {
            String currentCorrectAnswerText = dto.getAnswers().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getCorrect()))
                    .map(AnswerDto::getText)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Đáp án đúng cho TRUE_FALSE phải có một đáp án đúng được chọn.")); // This should not happen if validateAnswersByType passed.

            // Additional check for the text value, as validateAnswersByType no longer does this.
            if (!"True".equalsIgnoreCase(currentCorrectAnswerText) && !"False".equalsIgnoreCase(currentCorrectAnswerText)
                    && !"Đúng".equalsIgnoreCase(currentCorrectAnswerText) && !"Sai".equalsIgnoreCase(currentCorrectAnswerText)) {
                throw new IllegalArgumentException("Đáp án đúng cho TRUE_FALSE phải là 'True', 'False', 'Đúng' hoặc 'Sai'.");
            }

            q.setCorrectAnswer(currentCorrectAnswerText); // e.g., "Đúng"

            // q.getAnswers().clear() was called before this if block, so directly add to it
            for (AnswerDto answerDto : dto.getAnswers()) { // Iterate through the DTO's answers
                Answer answer = new Answer();
                answer.setText(answerDto.getText()); // Use the text from DTO, e.g., "Đúng" or "Sai"
                // Set correct based on whether its text matches the currentCorrectAnswerText
                answer.setCorrect(answerDto.getText().equalsIgnoreCase(currentCorrectAnswerText));
                answer.setQuestion(q);
                q.getAnswers().add(answer);
            }
        } else {
            List<Answer> newAnswers = dto.getAnswers().stream().map(aDto -> {
                Answer a = new Answer();
                a.setText(aDto.getText());
                a.setCorrect(Boolean.TRUE.equals(aDto.getCorrect()));
                a.setQuestion(q);
                return a;
            }).collect(Collectors.toList());
            q.getAnswers().addAll(newAnswers);

            // New logic: Populate correct_answer for SINGLE/MULTIPLE
            List<String> correctTexts = dto.getAnswers().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getCorrect()))
                    .map(AnswerDto::getText)
                    .collect(Collectors.toList());
            q.setCorrectAnswer(String.join("||", correctTexts));
        }

        Question updatedQuestion = questionRepo.save(q);
        return populateCreatorInfo(entityDtoMapper.toQuestionResponseDto(updatedQuestion));
    }

    // DELETE
    @Override
    public void deleteQuestion(Long id, String actorUsername) {
        Question q = questionRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Câu hỏi không tồn tại"));
        ;

        // Check if user is admin or owner
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !q.getCreatedBy().equals(actorUsername)) {
            throw new SecurityException("Không có quyền xóa câu hỏi này.");
        }
        if (examQuestionRepo.existsByQuestionId(id)) {
            throw new IllegalStateException("Không thể xóa câu hỏi đã được chọn vào bài thi.");
        }
        // Decrement totalQuestions for the associated category
        Category category = q.getCategory();
        category.setTotalQuestions(category.getTotalQuestions() - 1);
        categoryRepo.save(category);

        questionRepo.delete(q);
    }

    // ADMIN METHODS - Full permissions
    @Override
    public QuestionResponseDto updateQuestionAsAdmin(Long id, QuestionUpdateDto dto) {
        Question q = questionRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Câu hỏi không tồn tại"));

        Category oldCategory = q.getCategory(); // Get the current category of the question

        QuestionType type = QuestionType.valueOf(dto.getType());
        validateAnswersByType(type, dto.getAnswers());

        Category newCategory = categoryRepo.findById(dto.getCategoryId()) // This is the NEW category
                .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));

        q.setTitle(dto.getTitle());
        q.setType(type);
        if (dto.getVisibility() != null && !dto.getVisibility().isBlank()) {
            q.setVisibility(QuestionVisibility.valueOf(dto.getVisibility().toUpperCase()));
        }
        q.setDifficulty(dto.getDifficulty());
        q.setCategory(newCategory); // Set the new category

        // Check if category has changed
        if (!oldCategory.getId().equals(newCategory.getId())) {
            // Decrement old category count
            oldCategory.setTotalQuestions(oldCategory.getTotalQuestions() - 1);
            categoryRepo.save(oldCategory);

            // Increment new category count
            newCategory.setTotalQuestions(newCategory.getTotalQuestions() + 1);
            categoryRepo.save(newCategory);
        }

        // Replace answers
        q.getAnswers().clear();
        if (type == QuestionType.TRUE_FALSE) {
            String currentCorrectAnswerText = dto.getAnswers().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getCorrect()))
                    .map(AnswerDto::getText)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Đáp án đúng cho TRUE_FALSE phải có một đáp án đúng được chọn.")); // This should not happen if validateAnswersByType passed.

            // Additional check for the text value, as validateAnswersByType no longer does this.
            if (!"True".equalsIgnoreCase(currentCorrectAnswerText) && !"False".equalsIgnoreCase(currentCorrectAnswerText)
                    && !"Đúng".equalsIgnoreCase(currentCorrectAnswerText) && !"Sai".equalsIgnoreCase(currentCorrectAnswerText)) {
                throw new IllegalArgumentException("Đáp án đúng cho TRUE_FALSE phải là 'True', 'False', 'Đúng' hoặc 'Sai'.");
            }

            q.setCorrectAnswer(currentCorrectAnswerText); // e.g., "Đúng"

            // q.getAnswers().clear() was called before this if block, so directly add to it
            for (AnswerDto answerDto : dto.getAnswers()) { // Iterate through the DTO's answers
                Answer answer = new Answer();
                answer.setText(answerDto.getText()); // Use the text from DTO, e.g., "Đúng" or "Sai"
                // Set correct based on whether its text matches the currentCorrectAnswerText
                answer.setCorrect(answerDto.getText().equalsIgnoreCase(currentCorrectAnswerText));
                answer.setQuestion(q);
                q.getAnswers().add(answer);
            }
        } else {
            List<Answer> newAnswers = dto.getAnswers().stream().map(aDto -> {
                Answer a = new Answer();
                a.setText(aDto.getText());
                a.setCorrect(Boolean.TRUE.equals(aDto.getCorrect()));
                a.setQuestion(q);
                return a;
            }).collect(Collectors.toList());
            q.getAnswers().addAll(newAnswers);

            // New logic: Populate correct_answer for SINGLE/MULTIPLE
            List<String> correctTexts = dto.getAnswers().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getCorrect()))
                    .map(AnswerDto::getText)
                    .collect(Collectors.toList());
            q.setCorrectAnswer(String.join("||", correctTexts));
        }

        Question updatedQuestion = questionRepo.save(q);
        return populateCreatorInfo(entityDtoMapper.toQuestionResponseDto(updatedQuestion));
    }

    @Override
    public void deleteQuestionAsAdmin(Long id) {
        Question q = questionRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Câu hỏi không tồn tại"));

        if (examQuestionRepo.existsByQuestionId(id)) {
            throw new IllegalStateException("Không thể xóa câu hỏi đã được chọn vào bài thi.");
        }

        // Admin can delete any question - no ownership check
        // Decrement totalQuestions for the associated category
        Category category = q.getCategory();
        category.setTotalQuestions(category.getTotalQuestions() - 1);
        categoryRepo.save(category);

        questionRepo.delete(q);
    }

    // SEARCH
    @Override
    public Page<QuestionResponseDto> searchQuestions(String keyword, String difficulty, String type, Long categoryId,
            String createdBy, String visibility, String currentUsername, Pageable pageable) {

        com.example.final_project.entity.QuestionVisibility visEnum = null;
        if (visibility != null && !visibility.isEmpty()) {
            try {
                visEnum = com.example.final_project.entity.QuestionVisibility.valueOf(visibility.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid visibility
            }
        }

        com.example.final_project.entity.QuestionType typeEnum = null;
        if (type != null && !type.isEmpty()) {
            try {
                typeEnum = com.example.final_project.entity.QuestionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid type
            }
        }

        // Check if user is admin
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Page<Question> questionPage = questionRepo.searchQuestions(keyword, difficulty, typeEnum, categoryId, createdBy,
                visEnum, currentUsername, isAdmin, pageable);
        return questionPage.map(entityDtoMapper::toQuestionResponseDto).map(this::populateCreatorInfo);
    }

    // Validator function
    private void validateAnswersByType(QuestionType type, List<AnswerDto> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("Phải cung cấp ít nhất một đáp án.");
        }
        long correctCount = answers.stream().filter(a -> Boolean.TRUE.equals(a.getCorrect())).count();

        switch (type) {
            case SINGLE:
                if (correctCount != 1)
                    throw new IllegalArgumentException("Loại " + type + " phải có đúng 1 đáp án đúng.");
                break;
            case MULTIPLE:
                if (correctCount < 2)
                    throw new IllegalArgumentException("Loại MULTIPLE phải có ít nhất 2 đáp án đúng.");
                break;
            case TRUE_FALSE:
                if (correctCount != 1) {
                    throw new IllegalArgumentException("Loại TRUE_FALSE phải có đúng 1 đáp án đúng.");
                }
                break;
        }
    }

    private QuestionResponseDto populateCreatorInfo(QuestionResponseDto dto) {
        if (dto == null)
            return null;
        String email = dto.getCreatedBy();
        dto.setCreatedByRole(resolveRole(email));
        dto.setCreatedByName(resolveName(email));
        return dto;
    }

    private String resolveRole(String email) {
        if (email == null || email.isBlank())
            return null;
        if (teacherRepo.findByEmail(email).isPresent())
            return "teacher";
        if (adminRepo.findByEmail(email).isPresent())
            return "admin";
        return null;
    }

    private String resolveName(String email) {
        if (email == null || email.isBlank())
            return null;
        var teacherOpt = teacherRepo.findByEmail(email);
        if (teacherOpt.isPresent()) {
            return teacherOpt.get().getUsername();
        }
        var adminOpt = adminRepo.findByEmail(email);
        if (adminOpt.isPresent()) {
            return adminOpt.get().getUsername();
        }
        return "unknown";
    }
}