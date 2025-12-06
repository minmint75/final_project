package com.example.final_project.service;

import com.example.final_project.dto.WaitingRoomUserDto;

import java.util.List;
import java.util.Optional;

public interface WaitingRoomService {
    void addUser(String accessCode, WaitingRoomUserDto user);
    void removeUser(String accessCode, Long userId);
    List<WaitingRoomUserDto> getParticipants(String accessCode);
    Optional<String> getAccessCodeByUserId(Long userId);
    void removeUserFromAllRooms(Long userId);
}
