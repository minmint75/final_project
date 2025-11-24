package com.example.final_project.controller;

import com.example.final_project.dto.*;
import com.example.final_project.entity.Question;
import com.example.final_project.service.QuestionService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Data
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService QuestionService;

    // --- 1. POST: Tạo mới câu hỏi ---
    // Frontend gọi: POST /api/questions
    @PostMapping
    public ResponseEntity<Question> create(@Valid @RequestBody com.example.final_project.dto.QuestionCreateDto dto) {
        // Trong Service, bạn sẽ lấy thông tin User ID từ SecurityContext (hoặc Token)
        Question q = QuestionService.createQuestion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(q);
    }

    // --- 2. GET: Lấy chi tiết câu hỏi (Dùng cho trang Sửa) ---
    // Frontend gọi: GET /api/questions/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Question> get(@PathVariable Long id) {
        // Service nên kiểm tra quyền đọc nếu cần
        Question question = QuestionService.getQuestionById(id);
        return ResponseEntity.ok(question);
    }

    // --- 3. GET: Danh sách và Tìm kiếm (Endpoint chính cho bảng) ---
    // Frontend gọi: GET /api/questions/search?page=0&size=20&search=...
    @GetMapping("/search")
    public ResponseEntity<Page<Question>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, // SỬA: Lấy size từ tham số
            @RequestParam(required = false) String search, // Tham số tìm kiếm chung (tiêu đề/đáp án)
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category
            // Bạn có thể bỏ tham số sort và để Service xử lý sorting mặc định
    ) {
        // SỬA: Chuyển các tham số tìm kiếm và phân trang vào Service
        Page<Question> p = QuestionService.searchQuestions(page, size, search, difficulty, type, category);
        return ResponseEntity.ok(p);
    }

    // --- 4. PUT: Cập nhật câu hỏi ---
    // Frontend gọi: PUT /api/questions/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Question> update(
            @PathVariable Long id,
            @Valid @RequestBody com.example.final_project.dto.QuestionUpdateDto dto,
            @RequestHeader(value = "X-User-Id", required = false) String actorId // SỬA: Dùng ID
    ) {
        // Tốt nhất nên lấy thông tin người dùng từ SecurityContext, không phải header
        if (actorId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User authentication required");
        }

        // Logic phân quyền (ví dụ: chỉ Admin hoặc người tạo mới được Sửa) nên nằm trong Service
        Question updated = QuestionService.updateQuestion(id, dto, actorId);
        return ResponseEntity.ok(updated);
    }

    // --- 5. DELETE: Xóa câu hỏi ---
    // Frontend gọi: DELETE /api/questions/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String actorId // SỬA: Dùng ID
    ) {
        if (actorId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User authentication required");
        }

        // Logic phân quyền (Admin hoặc người tạo) nằm trong Service
        QuestionService.deleteQuestion(id, actorId);
        return ResponseEntity.noContent().build();
    }

    public QuestionService getQuestionService() {
        return QuestionService;
    }

    public void setQuestionService(QuestionService questionService) {
        QuestionService = questionService;
    }
}
