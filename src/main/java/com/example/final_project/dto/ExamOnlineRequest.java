package com.example.final_project.dto;

import com.example.final_project.entity.ExamLevel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamOnlineRequest {

    @NotBlank(message = "Tên bài thi không được để trống")
    private String name;

    @NotNull(message = "Số lượng câu hỏi không được để trống")
    @Min(value = 1, message = "Số lượng câu hỏi phải lớn hơn 0")
    private Integer numberOfQuestions;

    @NotNull(message = "Loại đề thi không được để trống")
    private ExamLevel level;

    @NotNull(message = "Thời gian nộp bài không được để trống")
    @Future(message = "Thời gian nộp bài phải ở thì tương lai")
    private LocalDateTime submissionDeadline;

    @NotNull(message = "Điểm đạt không được để trống")
    @Min(value = 0, message = "Điểm đạt không được nhỏ hơn 0")
    private Integer passingScore;

    @NotNull(message = "Số người tối đa không được để trống")
    @Min(value = 1, message = "Số người tối đa phải lớn hơn 0")
    private Integer maxParticipants;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;
}
