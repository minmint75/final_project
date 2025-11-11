package com.example.final_project.service;

import com.example.final_project.dto.ProfileUpdateRequest;

import java.util.Map;

public interface ProfileService {
    void updateProfile(String currentUsername, ProfileUpdateRequest request);
    Map<String, Object> getProfile(String currentUsername);
    void deleteAvatar(String currentUsername);
}
