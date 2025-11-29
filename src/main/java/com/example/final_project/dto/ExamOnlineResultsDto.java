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
public class ExamOnlineResultsDto {

    private String examName;
    private long numberOfParticipants;
    private long numberOfSubmissions;
    private List<StudentResult> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentResult {
        private Long studentId;
        private String avatarUrl;
        private String displayName;
        private Double score;
        private Integer correctCount;
        private Integer totalQuestions;
        private Integer attemptNumber;
    }
}
