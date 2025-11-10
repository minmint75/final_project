package com.example.final_project.controller;

import com.example.final_project.dto.*;
import com.example.final_project.entity.Question;
import com.example.final_project.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody QuestionCreateDto dto) {
        Question q = questionService.createQuestion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(q);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Question>> list(@RequestParam(defaultValue = "0") int page) {
        Page<Question> p = questionService.getAllQuestions(page, 10);
        return ResponseEntity.ok(p);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody QuestionUpdateDto dto,
                                    @RequestHeader(value = "X-User", required = false) String actor) {
        if (actor == null) actor = dto.getAnswers().isEmpty() ? "unknown" : "unknown";
        Question updated = questionService.updateQuestion(id, dto, actor);
        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @RequestHeader(value = "X-User", required = false) String actor) {
        if (actor == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing X-User header");
        questionService.deleteQuestion(id, actor);
        return ResponseEntity.noContent().build();
    }
}