package com.example.final_project.dto;

import com.example.final_project.entity.ExamLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExamOnlineRequest {

    @NotBlank(message = "Tên bài thi không được để trống")
    private String name;

    @NotNull(message = "Loại đề thi không được để trống")
    private ExamLevel level;

    @NotNull(message = "Thời gian làm bài không được để trống")
    @Min(value = 1, message = "Thời gian làm bài phải lớn hơn 0")
    private Integer durationMinutes; // Thời gian làm bài tính bằng phút

    @NotNull(message = "Điểm đạt không được để trống")
    @Min(value = 0, message = "Điểm đạt không được nhỏ hơn 0")
    private Integer passingScore;

    @NotNull(message = "Số người tối đa không được để trống")
    @Min(value = 1, message = "Số người tối đa phải lớn hơn 0")
    private Integer maxParticipants;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;
}
