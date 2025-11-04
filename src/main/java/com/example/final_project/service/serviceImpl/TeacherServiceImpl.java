package com.example.final_project.service.serviceImpl;

import com.example.final_project.entity.Teacher;
import com.example.final_project.repository.TeacherRepository;
import com.example.final_project.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;

    @Autowired
    public TeacherServiceImpl(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    @Override
    public Page<Teacher> searchTeachers(String keyword, Pageable pageable) {
        return teacherRepository.searchTeachers(keyword, pageable);
    }

    @Override
    public Optional<Teacher> findByUsername(String username) {
        return teacherRepository.findByUsername(username);
    }

    @Override
    public Page<Teacher> findByEmail(String email, Pageable pageable) {
        return teacherRepository.findByEmail(email, pageable);
    }

    @Override
    public Optional<Teacher> findByEmail(String email) {
        return teacherRepository.findByEmail(email);
    }

    @Override
    public Optional<Teacher> findById(Long id) {
        return teacherRepository.findById(id);
    }

    @Override
    public Teacher save(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    @Override
    public void deleteById(Long id) {
        teacherRepository.deleteById(id);
    }
}
