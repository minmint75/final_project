package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.*;
import com.example.final_project.entity.*;
import com.example.final_project.repository.*;
import com.example.final_project.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionRepository questionRepo;
    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private ExamQuestionRepository examQuestionRepo;

    // CREATE
    @Override
    public Question createQuestion(QuestionCreateDto dto) {
        QuestionType type = QuestionType.valueOf(dto.getType());
        validateAnswersByType(type, dto.getAnswers());

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));

        Question q = new Question();
        q.setTitle(dto.getTitle());
        q.setType(type);
        q.setDifficulty(dto.getDifficulty());
        q.setCategory(category);
        q.setCreatedBy(dto.getCreatedBy());

        List<Answer> answers = dto.getAnswers().stream().map(aDto -> {
            Answer a = new Answer();
            a.setText(aDto.getText());
            a.setCorrect(Boolean.TRUE.equals(aDto.getCorrect()));
            a.setQuestion(q);
            return a;
        }).collect(Collectors.toList());
        q.setAnswers(answers);

        return questionRepo.save(q);
    }

    // GET SINGLE
    @Override
    public Question getQuestionById(Long id) {
        return questionRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Câu hỏi không tồn tại"));
    }

    // LIST (paged, newest first)
    @Override
    public Page<Question> getAllQuestions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return questionRepo.findAll(pageable);
    }

    // UPDATE
    @Override
    public Question updateQuestion(Long id, QuestionUpdateDto dto, String actorUsername) {
        Question q = getQuestionById(id);

        // Check if already used in exam -> block update
        if (examQuestionRepo.existsByQuestionId(id)) {
            throw new IllegalStateException("Không thể cập nhật câu hỏi đã được chọn vào bài thi.");
        }

        // Optional: only allow owner to edit
        if (!q.getCreatedBy().equals(actorUsername)) {
            throw new SecurityException("Không có quyền cập nhật câu hỏi này.");
        }

        QuestionType type = QuestionType.valueOf(dto.getType());
        validateAnswersByType(type, dto.getAnswers());

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));

        q.setTitle(dto.getTitle());
        q.setType(type);
        q.setDifficulty(dto.getDifficulty());
        q.setCategory(category);

        // Replace answers
        q.getAnswers().clear();
        List<Answer> newAnswers = dto.getAnswers().stream().map(aDto -> {
            Answer a = new Answer();
            a.setText(aDto.getText());
            a.setCorrect(Boolean.TRUE.equals(aDto.getCorrect()));
            a.setQuestion(q);
            return a;
        }).collect(Collectors.toList());
        q.getAnswers().addAll(newAnswers);

        return questionRepo.save(q);
    }

    // DELETE
    @Override
    public void deleteQuestion(Long id, String actorUsername) {
        Question q = getQuestionById(id);

        if (!q.getCreatedBy().equals(actorUsername)) {
            throw new SecurityException("Không có quyền xóa câu hỏi này.");
        }
        if (examQuestionRepo.existsByQuestionId(id)) {
            throw new IllegalStateException("Không thể xóa câu hỏi đã được chọn vào bài thi.");
        }
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
            case TRUE_FALSE:
                if (correctCount != 1) throw new IllegalArgumentException("Loại " + type + " phải có đúng 1 đáp án đúng.");
                break;
            case MULTIPLE:
                if (correctCount < 2) throw new IllegalArgumentException("Loại MULTIPLE phải có ít nhất 2 đáp án đúng.");
                break;
        }
    }
}