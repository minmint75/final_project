package com.example.final_project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySearchRequest {
    private String name;

    private int page = 0;
    private int size = 20;

    // Sort mặc định theo id để bản ghi mới (id lớn hơn) đứng trước khi direction = desc
    private String sort = "id";
    private String direction = "desc";

    public boolean hasFilters() {
        return (name != null && !name.trim().isEmpty());
    }

    public String getSortDirection() {
        return "asc".equalsIgnoreCase(direction) ? "asc" : "desc";
    }

    public boolean isAscending() {
        return "asc".equalsIgnoreCase(direction);
    }
}