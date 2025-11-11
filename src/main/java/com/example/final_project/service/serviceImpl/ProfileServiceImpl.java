package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.ProfileUpdateRequest;
import com.example.final_project.entity.Admin;
import com.example.final_project.entity.Student;
import com.example.final_project.entity.Teacher;
import com.example.final_project.repository.AdminRepository;
import com.example.final_project.repository.StudentRepository;
import com.example.final_project.repository.TeacherRepository;
import com.example.final_project.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    @Transactional
    public void updateProfile(String currentUsername, ProfileUpdateRequest request) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(currentUsername)
                .or(() -> adminRepository.findByUsername(currentUsername));
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            updateUsernameAndAvatar(admin, request.getUsername(), request.getAvatar());
            adminRepository.save(admin);
            return;
        }

        Optional<Teacher> teacherOpt = teacherRepository.findByEmail(currentUsername)
                .or(() -> teacherRepository.findByUsername(currentUsername));
        if (teacherOpt.isPresent()) {
            Teacher teacher = teacherOpt.get();
            updateUsernameAndAvatar(teacher, request.getUsername(), request.getAvatar());
            teacherRepository.save(teacher);
            return;
        }

        Optional<Student> studentOpt = studentRepository.findByEmail(currentUsername)
                .or(() -> studentRepository.findByUsername(currentUsername));
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            updateUsernameAndAvatar(student, request.getUsername(), request.getAvatar());
            studentRepository.save(student);
            return;
        }

        throw new UsernameNotFoundException("User not found: " + currentUsername);
    }

    @Override
    public Map<String, Object> getProfile(String currentUsername) {
        Map<String, Object> profile = new HashMap<>();

        Optional<Admin> adminOpt = adminRepository.findByEmail(currentUsername)
                .or(() -> adminRepository.findByUsername(currentUsername));
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            profile.put("username", admin.getUsername());
            profile.put("email", admin.getEmail());
            profile.put("avatar", admin.getAvatar());
            profile.put("role", "ADMIN");
            return profile;
        }

        Optional<Teacher> teacherOpt = teacherRepository.findByEmail(currentUsername)
                .or(() -> teacherRepository.findByUsername(currentUsername));
        if (teacherOpt.isPresent()) {
            Teacher teacher = teacherOpt.get();
            profile.put("username", teacher.getUsername());
            profile.put("email", teacher.getEmail());
            profile.put("avatar", teacher.getAvatar());
            profile.put("role", "TEACHER");
            return profile;
        }

        Optional<Student> studentOpt = studentRepository.findByEmail(currentUsername)
                .or(() -> studentRepository.findByUsername(currentUsername));
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            profile.put("username", student.getUsername());
            profile.put("email", student.getEmail());
            profile.put("avatar", student.getAvatar());
            profile.put("role", "STUDENT");
            return profile;
        }

        throw new UsernameNotFoundException("User not found: " + currentUsername);
    }

    @Override
    public void deleteAvatar(String currentUsername) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(currentUsername)
                .or(() -> adminRepository.findByUsername(currentUsername));
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            admin.setAvatar(null);
            adminRepository.save(admin);
            return;
        }

        Optional<Teacher> teacherOpt = teacherRepository.findByEmail(currentUsername)
                .or(() -> teacherRepository.findByUsername(currentUsername));
        if (teacherOpt.isPresent()) {
            Teacher teacher = teacherOpt.get();
            teacher.setAvatar(null);
            teacherRepository.save(teacher);
            return;
        }

        Optional<Student> studentOpt = studentRepository.findByEmail(currentUsername)
                .or(() -> studentRepository.findByUsername(currentUsername));
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            student.setAvatar(null);
            studentRepository.save(student);
            return;
        }

        throw new UsernameNotFoundException("User not found: " + currentUsername);
    }

    private void updateUsernameAndAvatar(Object user, String newUsername, String newAvatar) {
        if (user instanceof Admin) {
            if (newUsername != null && !newUsername.trim().isEmpty()) {
                ((Admin) user).setUsername(newUsername);
            }
            ((Admin) user).setAvatar(newAvatar);
        } else if (user instanceof Teacher) {
            if (newUsername != null && !newUsername.trim().isEmpty()) {
                ((Teacher) user).setUsername(newUsername);
            }
            ((Teacher) user).setAvatar(newAvatar);
        } else if (user instanceof Student) {
            if (newUsername != null && !newUsername.trim().isEmpty()) {
                ((Student) user).setUsername(newUsername);
            }
            ((Student) user).setAvatar(newAvatar);
        }
    }
}
