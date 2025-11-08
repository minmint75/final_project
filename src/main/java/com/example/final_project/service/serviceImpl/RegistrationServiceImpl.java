package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.RegistrationRequest;
import com.example.final_project.entity.Student;
import com.example.final_project.entity.Teacher;
import com.example.final_project.repository.StudentRepository;
import com.example.final_project.repository.TeacherRepository;
import com.example.final_project.service.EmailService;
import com.example.final_project.service.RegistrationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public void register(RegistrationRequest request) {
        // Check if email exists in Teacher repository
        Optional<Teacher> existingTeacher = teacherRepository.findByEmail(request.getEmail());
        if (existingTeacher.isPresent()) {
            Teacher teacher = existingTeacher.get();
            if (teacher.getStatus() == Teacher.TeacherStatus.PENDING) {
                throw new IllegalArgumentException("Tài khoản giáo viên này đang chờ phê duyệt. Vui lòng đợi hoặc liên hệ quản trị viên.");
            } else if (teacher.getStatus() == Teacher.TeacherStatus.REJECTED) {
                throw new IllegalArgumentException("Email này đã bị từ chối. Vui lòng sử dụng email khác.");
            } else {
                throw new IllegalArgumentException("Email đã tồn tại.");
            }
        }

        // Check if email exists in Student repository
        Optional<Student> existingStudent = studentRepository.findByEmail(request.getEmail());
        if (existingStudent.isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }

        switch (request.getRole()) {
            case STUDENT:
                Student student = new Student();
                student.setUsername(request.getUsername());
                student.setPassword(passwordEncoder.encode(request.getPassword()));
                student.setEmail(request.getEmail());
                student.setStatus(Student.StudentStatus.ACTIVE);
                studentRepository.save(student);
                emailService.sendRegistrationSuccessEmail(student.getEmail());
                break;
            case TEACHER:
                Teacher teacher = new Teacher();
                teacher.setUsername(request.getUsername().trim());
                teacher.setPassword(passwordEncoder.encode(request.getPassword()));
                teacher.setEmail(request.getEmail().trim());
                teacher.setStatus(Teacher.TeacherStatus.PENDING);
                teacher.setRoleName(com.example.final_project.entity.RoleName.TEACHER);
                teacherRepository.save(teacher);
                emailService.sendTeacherPendingEmail(teacher.getEmail());
                break;
            default:
                throw new IllegalArgumentException("Invalid role specified for registration.");
        }
    }
}
