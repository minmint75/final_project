package com.example.final_project.dto;

import com.example.final_project.entity.ExamLevel;
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

    private List<Long> questionIds;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "Mức độ đề thi không được để trống")
    private ExamLevel examLevel;
}