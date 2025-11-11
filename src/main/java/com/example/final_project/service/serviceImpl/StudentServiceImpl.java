package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.StudentSearchRequest;
import com.example.final_project.entity.Student;
import com.example.final_project.repository.StudentRepository;
import com.example.final_project.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Override
    public Page<Student> searchStudent(String keyword, Pageable pageable) {
        return studentRepository.searchStudents(keyword, pageable);
    }

    public Page<Student> searchStudent(StudentSearchRequest request) {

        // 1️⃣ Tạo đối tượng Sort theo hướng asc/desc
        Sort sort = Sort.by(
                request.isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC,
                request.getSort()
        );

        // 2️⃣ Tạo Pageable từ thông tin request
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort
        );

        // 3️⃣ Ghép từ khóa tìm kiếm: ưu tiên username > email
        String keyword = null;
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            keyword = request.getUsername();
        } else if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            keyword = request.getEmail();
        }

        // 4️⃣ Nếu không có bộ lọc, trả về tất cả (gọi repository tìm tất cả)
        if (keyword == null) {
            return studentRepository.findAll(pageable);
        }

        return searchStudent(keyword, pageable);
    }

    @Override
    public Page<Student> findByEmail(String email, Pageable pageable) {
        return studentRepository.findByEmail(email, pageable);
    }

    @Override
    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    @Override
    public Student save(Student student) {
        return studentRepository.save(student);
    }

    @Override
    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }

    @Override
    public Optional<Student> findByUsername(String username) {
        return studentRepository.findByUsername(username);
    }

    @Override
    public Optional<Student> findByEmail(String email) {
        return studentRepository.findByEmail(email);
    }
    
    @Override
    public Page<Student> searchByEmail(String email, Pageable pageable) {
        return studentRepository.searchByEmail(email, pageable);
    }
    
    @Override
    public Page<Student> searchByUsername(String username, Pageable pageable) {
        return studentRepository.searchByUsername(username, pageable);
    }
    
    @Override
    public Page<Student> searchByEmailAndUsername(String email, String username, Pageable pageable) {
        return studentRepository.searchByEmailAndUsername(email, username, pageable);
    }
}
