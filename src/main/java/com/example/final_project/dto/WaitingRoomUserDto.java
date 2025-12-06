package com.example.final_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitingRoomUserDto {
    private Long userId;
    private String displayName;
    private String avatar;
}
