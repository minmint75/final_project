package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.RegistrationRequest;
import com.example.final_project.entity.Student;
import com.example.final_project.entity.Teacher;
import com.example.final_project.repository.StudentRepository;
import com.example.final_project.repository.TeacherRepository;
import com.example.final_project.service.EmailService;
import com.example.final_project.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    public void register(RegistrationRequest request) {
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
                teacher.setUsername(request.getUsername());
                teacher.setPassword(passwordEncoder.encode(request.getPassword()));
                teacher.setEmail(request.getEmail());
                teacher.setStatus(Teacher.TeacherStatus.PENDING);
                teacherRepository.save(teacher);
                // Notify admin for approval
                break;
            default:
                throw new IllegalArgumentException("Invalid role specified for registration.");
        }
    }
}
