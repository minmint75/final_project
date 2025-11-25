package com.example.final_project.controller;

import com.example.final_project.dto.ExamDetailDto;
import com.example.final_project.dto.ExamRequestDto;
import com.example.final_project.entity.Exam;
import com.example.final_project.entity.Teacher;
import com.example.final_project.repository.TeacherRepository;
import com.example.final_project.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER'), hasRole('ADMIN')")
public class ExamController {

    private final ExamService examService;
    private final TeacherRepository teacherRepository;

    @PostMapping
    public ResponseEntity<Exam> createExam(@Valid @RequestBody ExamRequestDto dto, Principal principal) {
        String email = principal.getName();
        Teacher teacher = teacherRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Teacher not found"));
        Exam exam = examService.createExam(dto, teacher.getTeacherId());
        return ResponseEntity.ok(exam);
    }

    @PutMapping("/update/{examId}")
    public ResponseEntity<Exam> updateExam(
            @PathVariable Long examId,
            @Valid @RequestBody ExamRequestDto dto,
            Principal principal) {
        String email = principal.getName();
        Teacher teacher = teacherRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Teacher not found"));
        Exam exam = examService.updateExam(examId, dto, teacher.getTeacherId());
        return ResponseEntity.ok(exam);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Exam>> getMyExams(Principal principal) {
        String email = principal.getName();
        Teacher teacher = teacherRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Teacher not found"));
        List<Exam> exams = examService.getExamsByTeacher(teacher.getTeacherId());
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/{examId}")
    public ResponseEntity<ExamDetailDto> getExam(@PathVariable Long examId, Principal principal) {
        String email = principal.getName();
        Teacher teacher = teacherRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Teacher not found"));
        Exam exam = examService.getExamById(examId, teacher.getTeacherId());
        
        // Convert to DTO
        ExamDetailDto dto = ExamDetailDto.builder()
                .examId(exam.getExamId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .durationMinutes(exam.getDurationMinutes())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .category(exam.getCategory() != null ? ExamDetailDto.CategoryDto.builder()
                        .categoryId(exam.getCategory().getId())
                        .name(exam.getCategory().getName())
                        .build() : null)
                .examQuestions(exam.getExamQuestions() != null ? exam.getExamQuestions().stream()
                        .map(eq -> ExamDetailDto.ExamQuestionDto.builder()
                                .questionId(eq.getQuestion().getId())
                                .title(eq.getQuestion().getTitle())
                                .difficulty(eq.getQuestion().getDifficulty())
                                .answers(eq.getQuestion().getAnswers() != null ? 
                                        eq.getQuestion().getAnswers().stream()
                                        .map(a -> ExamDetailDto.AnswerDto.builder()
                                                .answerId(a.getId())
                                                .answerText(a.getText())
                                                .isCorrect(a.isCorrect())
                                                .build())
                                        .toList() : List.of())
                                .build())
                        .toList() : List.of())
                .build();
        
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/delete/{examId}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long examId, Principal principal) {
        try {
            String email = principal.getName();
            Teacher teacher = teacherRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Teacher not found"));
            examService.deleteExamById(examId, teacher.getTeacherId());
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting exam", e);
        }
    }
}