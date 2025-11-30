package com.example.final_project.controller;

import com.example.final_project.dto.*;
import com.example.final_project.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping("/create")
    public ResponseEntity<QuestionResponseDto> create(@Valid @RequestBody QuestionCreateDto dto, Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        // Set createdBy from authenticated user
        String currentUser = principal.getName();
        dto.setCreatedBy(currentUser);
        QuestionResponseDto q = questionService.createQuestion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(q);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<QuestionResponseDto>> searchQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q, // Keyword (nếu Frontend gửi)
            @RequestParam(required = false) String difficulty, // Bộ lọc độ khó
            @RequestParam(required = false) String type, // Bộ lọc loại
            @RequestParam(required = false) Long categoryId, // Bộ lọc danh mục
            @RequestParam(required = false) String createdBy, // Lọc theo người tạo (nếu cần)
            Principal principal // Lấy thông tin người dùng
    ) {
        Page<QuestionResponseDto> p = questionService.searchAndFilterQuestions(
                q, difficulty, type, categoryId, createdBy, PageRequest.of(page, size)
        );
        return ResponseEntity.ok(p);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<QuestionResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<QuestionResponseDto>> list(@RequestParam(defaultValue = "0") int page) {
        Page<QuestionResponseDto> p = questionService.getAllQuestions(page, 10);
        return ResponseEntity.ok(p);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<QuestionResponseDto>> getMyQuestions(
            @RequestParam(defaultValue = "0") int page, 
            Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String currentUser = principal.getName();
        Page<QuestionResponseDto> p = questionService.getQuestionsByUser(currentUser, page, 10);
        return ResponseEntity.ok(p);
    }

    @PatchMapping("/edit/{id}")
    public ResponseEntity<QuestionResponseDto> update(@PathVariable Long id,
                                    @Valid @RequestBody QuestionUpdateDto dto,
                                    Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String currentUser = principal.getName();
        QuestionResponseDto updated = questionService.updateQuestion(id, dto, currentUser);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String currentUser = principal.getName();
        questionService.deleteQuestion(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/question-types")
    public ResponseEntity<com.example.final_project.entity.QuestionType[]> getQuestionTypes() {
        return ResponseEntity.ok(com.example.final_project.entity.QuestionType.values());
    }

    @GetMapping("/difficulties")
    public ResponseEntity<java.util.List<String>> getDifficulties() {
        return ResponseEntity.ok(java.util.Arrays.asList("Easy", "Medium", "Hard"));
    }
}