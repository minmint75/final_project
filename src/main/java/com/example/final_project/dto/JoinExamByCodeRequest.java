package com.example.final_project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinExamByCodeRequest {
    @NotBlank(message = "Mã code không được để trống")
    private String code;
}
