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

    private String sort = "createdAt";
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