package com.example.final_project.dto;

import lombok.Data;
import java.util.List;

@Data
public class AllowedStudentsRequest {
    private List<String> studentEmails;
}
