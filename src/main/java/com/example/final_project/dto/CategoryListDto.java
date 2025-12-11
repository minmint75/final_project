package com.example.final_project.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryListDto {

    @NotBlank
    private Long id;
    private String name;
    private String description;
    private String createdByRole; 
    private String createdByName; 

    // @NotNull
    // private long questionId;
    // private long examId;
}
