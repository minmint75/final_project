package com.example.final_project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDto {
    private Long id;

    @NotBlank(message = "Nội dung đáp án không được bỏ trống")
    private String text;

    @NotNull(message = "Phải xác định đúng/sai cho đáp án")
    private Boolean correct;
}