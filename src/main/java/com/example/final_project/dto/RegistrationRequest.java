package com.example.final_project.dto;

import com.example.final_project.entity.RoleName;
import lombok.Data;

@Data
public class RegistrationRequest {
    private String username;
    private String password;
    private String email;
    private RoleName role;
}
