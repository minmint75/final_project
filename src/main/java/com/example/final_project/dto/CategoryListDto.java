package com.example.final_project.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryListDto {

    private Long id;
    private String name;
    private String description;
    private String createdByRole;
    private String createdByName;
    private String createdBy;
    private int questionCount;

    // @NotNull
    // private long questionId;
    // private long examId;
}
