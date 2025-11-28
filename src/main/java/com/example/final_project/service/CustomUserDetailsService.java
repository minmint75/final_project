package com.example.final_project.service;

import com.example.final_project.entity.RoleName;
import com.example.final_project.entity.Teacher;
import com.example.final_project.entity.Student;
import com.example.final_project.exception.AccountLockedException;
import com.example.final_project.repository.AdminRepository;
import com.example.final_project.repository.StudentRepository;
import com.example.final_project.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private StudentRepository studentRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<com.example.final_project.entity.Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            com.example.final_project.entity.Admin admin = adminOpt.get();
            System.out.println("DEBUG: User " + email + " found as Admin. Assigning ADMIN role.");
            return buildUserDetails(admin.getId(), admin.getEmail(), admin.getPassword(), RoleName.ADMIN);
        }

        Optional<com.example.final_project.entity.Teacher> teacherOpt = teacherRepository.findByEmail(email);
        if (teacherOpt.isPresent()) {
            com.example.final_project.entity.Teacher teacher = teacherOpt.get();
            
            // Check if teacher account is locked
            if (teacher.getStatus() == Teacher.TeacherStatus.LOCKED) {
                System.out.println("DEBUG: Teacher " + email + " is LOCKED. Denying access.");
                throw new AccountLockedException("Tài khoản giáo viên đã bị khóa. Vui lòng liên hệ quản trị viên.");
            }
            
            teacher.setLastVisit(java.time.LocalDateTime.now());
            teacherRepository.save(teacher);
            System.out.println("DEBUG: User " + email + " found as Teacher. Assigning TEACHER role.");
            return buildUserDetails(teacher.getTeacherId(), teacher.getEmail(), teacher.getPassword(), RoleName.TEACHER);
        }

        Optional<com.example.final_project.entity.Student> studentOpt = studentRepository.findByEmail(email);
        if (studentOpt.isPresent()) {
            com.example.final_project.entity.Student student = studentOpt.get();
            
            // Check if student account is locked
            if (student.getStatus() == Student.StudentStatus.LOCKED) {
                System.out.println("DEBUG: Student " + email + " is LOCKED. Denying access.");
                throw new AccountLockedException("Tài khoản học viên đã bị khóa. Vui lòng liên hệ quản trị viên.");
            }
            
            student.setLastVisit(java.time.LocalDateTime.now());
            studentRepository.save(student);
            System.out.println("DEBUG: User " + email + " found as Student. Assigning STUDENT role.");
            return buildUserDetails(student.getStudentId(), student.getEmail(), student.getPassword(), RoleName.STUDENT);
        }

        throw new UsernameNotFoundException("Không tìm thấy người dùng: " + email);
    }

    private UserDetails buildUserDetails(Long id, String email, String hashedPassword, RoleName role) {
        String roleWithPrefix = "ROLE_" + role.name();

        Set<GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority(roleWithPrefix)
        );

        System.out.println("DEBUG: Building CustomUserDetails for " + email + " with ID: " + id + " and Authority: " + roleWithPrefix);

        return new CustomUserDetails(
                id,
                email,
                hashedPassword,
                authorities
        );
    }
}