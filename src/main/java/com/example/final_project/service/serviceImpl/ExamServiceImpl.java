package com.example.final_project.service.impl;

import com.example.final_project.dto.ExamRequestDto;
import com.example.final_project.entity.*;
import com.example.final_project.repository.*;
import com.example.final_project.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final TeacherRepository teacherRepository;
    private final QuestionRepository questionRepository;
    private final ExamQuestionRepository examQuestionRepository;

    @Override
    @Transactional
    public Exam createExam(ExamRequestDto dto, Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên"));

        Exam exam = Exam.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .durationMinutes(dto.getDurationMinutes())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .teacher(teacher)
                .build();

        exam = examRepository.save(exam);

        // Thêm câu hỏi vào exam
        List<ExamQuestion> examQuestions = dto.getQuestionIds().stream().map(qid -> {
            Question question = questionRepository.findById(qid)
                    .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại: " + qid));
            return ExamQuestion.builder()
                    .exam(exam)
                    .question(question)
                    .orderIndex(0) // Có thể cải thiện sau
                    .build();
        }).collect(Collectors.toList());

        examQuestionRepository.saveAll(examQuestions);
        exam.setExamQuestions(examQuestions);

        return exam;
    }

    @Override
    @Transactional
    public Exam updateExam(Long examId, ExamRequestDto dto, Long teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài thi không tồn tại"));

        if (!exam.getTeacher().getTeacherId().equals(teacherId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bài thi này");
        }

        // Kiểm tra đã có người làm chưa
        if (examRepository.existsByExamIdAndExamQuestions_StudentAnswers_NotEmpty(examId)) {
            throw new RuntimeException("Không thể cập nhật bài thi đã có người làm");
        }

        exam.setTitle(dto.getTitle());
        exam.setDescription(dto.getDescription());
        exam.setDurationMinutes(dto.getDurationMinutes());
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());

        // Xóa câu hỏi cũ
        examQuestionRepository.deleteAll(exam.getExamQuestions());

        // Thêm câu hỏi mới
        List<ExamQuestion> newQuestions = dto.getQuestionIds().stream().map(qid -> {
            Question q = questionRepository.findById(qid)
                    .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại: " + qid));
            return ExamQuestion.builder().exam(exam).question(q).orderIndex(0).build();
        }).collect(Collectors.toList());

        examQuestionRepository.saveAll(newQuestions);
        exam.setExamQuestions(newQuestions);

        return examRepository.save(exam);
    }

    @Override
    public List<Exam> getExamsByTeacher(Long teacherId) {
        return examRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }

    @Override
    public Exam getExamById(Long examId, Long teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài thi không tồn tại"));
        if (!exam.getTeacher().getTeacherId().equals(teacherId)) {
            throw new RuntimeException("Bạn không có quyền xem bài thi này");
        }
        return exam;
    }
}