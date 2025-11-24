package com.example.final_project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCreateDto {

    @NotBlank(message = "Tiêu đề câu hỏi không được để trống")
    private String title;

    @NotBlank(message = "Loại câu hỏi là bắt buộc (Ví dụ: SINGLE, MULTIPLE, TRUE_FALSE)")
    private String type; // Tên của Enum QuestionType

    @NotBlank(message = "Độ khó là bắt buộc")
    private String difficulty;

    @NotNull(message = "Danh mục là bắt buộc")
    @Min(value = 1, message = "ID danh mục không hợp lệ")
    private Long categoryId;

    @NotNull(message = "Thông tin người tạo là bắt buộc")
    @NotBlank(message = "Tên người tạo không được để trống")
    private String createdBy; // Username/Email của người tạo

    @Valid // Kích hoạt validation trong AnswerDto
    @Size(min = 1, message = "Câu hỏi phải có ít nhất một đáp án")
    @NotNull(message = "Danh sách đáp án không được trống")
    private List<com.example.final_project.dto.AnswerDto> answers;
}