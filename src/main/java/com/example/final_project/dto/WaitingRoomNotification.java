package com.example.final_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitingRoomNotification {
    private String examName;
    private int participantCount;
    private List<WaitingRoomUserDto> participants;
    private String message;
}
