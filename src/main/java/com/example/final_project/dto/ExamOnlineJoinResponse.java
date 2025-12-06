package com.example.final_project.dto;

import com.example.final_project.entity.ExamStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamOnlineJoinResponse {
    private Long examOnlineId;
    private String name;
    private ExamStatus status;
    private int participantCount;
    private List<WaitingRoomUserDto> participants;
}

