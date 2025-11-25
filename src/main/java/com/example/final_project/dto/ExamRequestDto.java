package com.example.final_project.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamRequestDto {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    private String title;

    private String description;

    @NotNull(message = "Thời gian làm bài không được để trống")
    @Min(value = 1, message = "Thời gian làm bài phải lớn hơn 0")
    private Integer durationMinutes;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @NotNull(message = "Danh sách câu hỏi không được null")
    private List<Long> questionIds; // Có thể để mảng rỗng, cho phép tạo bài thi trước rồi thêm câu hỏi sau
}