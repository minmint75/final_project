package com.example.final_project.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ExamOnlineAddQuestionsRequest {

    @NotEmpty(message = "Danh sách câu hỏi không được để trống")
    private List<Long> questionIds;
}
