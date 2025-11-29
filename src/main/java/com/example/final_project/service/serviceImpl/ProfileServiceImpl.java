package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.ProfileUpdateRequest;
import com.example.final_project.entity.Admin;
import com.example.final_project.entity.Student;
import com.example.final_project.entity.Teacher;
import com.example.final_project.repository.AdminRepository;
import com.example.final_project.repository.StudentRepository;
import com.example.final_project.repository.TeacherRepository;
import com.example.final_project.service.FileStorageService;
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

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    @Transactional
    public void updateProfile(String currentUsername, ProfileUpdateRequest request) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(currentUsername)
                .or(() -> adminRepository.findByUsername(currentUsername));
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            String oldAvatar = admin.getAvatar();
            updateUsernameAndAvatar(admin, request.getUsername(), request.getAvatar());
            
            // Delete old avatar file if it's being replaced
            if (request.getAvatar() != null && oldAvatar != null && !oldAvatar.equals(request.getAvatar())) {
                String filename = oldAvatar.replace("/uploads/", "");
                fileStorageService.deleteFile(filename);
            }
            
            adminRepository.save(admin);
            return;
        }

        Optional<Teacher> teacherOpt = teacherRepository.findByEmail(currentUsername)
                .or(() -> teacherRepository.findByUsername(currentUsername));
        if (teacherOpt.isPresent()) {
            Teacher teacher = teacherOpt.get();
            String oldAvatar = teacher.getAvatar();
            updateUsernameAndAvatar(teacher, request.getUsername(), request.getAvatar());
            
            // Delete old avatar file if it's being replaced
            if (request.getAvatar() != null && oldAvatar != null && !oldAvatar.equals(request.getAvatar())) {
                String filename = oldAvatar.replace("/uploads/", "");
                fileStorageService.deleteFile(filename);
            }
            
            teacherRepository.save(teacher);
            return;
        }

        Optional<Student> studentOpt = studentRepository.findByEmail(currentUsername)
                .or(() -> studentRepository.findByUsername(currentUsername));
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            String oldAvatar = student.getAvatar();
            updateUsernameAndAvatar(student, request.getUsername(), request.getAvatar());
            
            // Delete old avatar file if it's being replaced
            if (request.getAvatar() != null && oldAvatar != null && !oldAvatar.equals(request.getAvatar())) {
                String filename = oldAvatar.replace("/uploads/", "");
                fileStorageService.deleteFile(filename);
            }
            
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
            profile.put("id", admin.getId());
            profile.put("name", admin.getUsername());
            profile.put("email", admin.getEmail());
            profile.put("avatar", admin.getAvatar());
            profile.put("role", "ADMIN");
            return profile;
        }

        Optional<Teacher> teacherOpt = teacherRepository.findByEmail(currentUsername)
                .or(() -> teacherRepository.findByUsername(currentUsername));
        if (teacherOpt.isPresent()) {
            Teacher teacher = teacherOpt.get();
            profile.put("id", teacher.getTeacherId());
            profile.put("name", teacher.getUsername());
            profile.put("email", teacher.getEmail());
            profile.put("avatar", teacher.getAvatar());
            profile.put("role", "TEACHER");
            return profile;
        }

        Optional<Student> studentOpt = studentRepository.findByEmail(currentUsername)
                .or(() -> studentRepository.findByUsername(currentUsername));
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            profile.put("id", student.getStudentId());
            profile.put("name", student.getUsername());
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
            String oldAvatar = admin.getAvatar();
            if (oldAvatar != null && !oldAvatar.trim().isEmpty()) {
                String filename = oldAvatar.replace("/uploads/", "");
                fileStorageService.deleteFile(filename);
            }
            admin.setAvatar(null);
            adminRepository.save(admin);
            return;
        }

        Optional<Teacher> teacherOpt = teacherRepository.findByEmail(currentUsername)
                .or(() -> teacherRepository.findByUsername(currentUsername));
        if (teacherOpt.isPresent()) {
            Teacher teacher = teacherOpt.get();
            String oldAvatar = teacher.getAvatar();
            if (oldAvatar != null && !oldAvatar.trim().isEmpty()) {
                String filename = oldAvatar.replace("/uploads/", "");
                fileStorageService.deleteFile(filename);
            }
            teacher.setAvatar(null);
            teacherRepository.save(teacher);
            return;
        }

        Optional<Student> studentOpt = studentRepository.findByEmail(currentUsername)
                .or(() -> studentRepository.findByUsername(currentUsername));
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            String oldAvatar = student.getAvatar();
            if (oldAvatar != null && !oldAvatar.trim().isEmpty()) {
                String filename = oldAvatar.replace("/uploads/", "");
                fileStorageService.deleteFile(filename);
            }
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
