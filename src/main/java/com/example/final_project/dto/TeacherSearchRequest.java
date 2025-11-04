package com.example.final_project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.example.final_project.entity.Teacher;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSearchRequest {
    private String username;
    private String email;

    private int page = 0;
    private int size = 20;

    private String sort = "createdAt";
    private String direction = "desc";

    public boolean hasFilters(){return (username != null || email != null);}
    public String getSortDirection() {
        return "asc".equalsIgnoreCase(direction) ? "asc" : "desc";
    }
    public boolean isAscending() {
        return "asc".equalsIgnoreCase(direction);
    }

}
