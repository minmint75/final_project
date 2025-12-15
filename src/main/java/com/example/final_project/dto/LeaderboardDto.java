package com.example.final_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardDto {
    private List<LeaderboardEntry> entries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardEntry {
        private Integer rank;
        private Long studentId;
        private String displayName;
        private String avatarUrl;
        private Double score;
        private Long timeSpent; // Seconds
    }
}
