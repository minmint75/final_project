package com.example.final_project.service;

import com.example.final_project.entity.RoleName;
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
            return buildUserDetails(admin.getEmail(), admin.getPassword(), RoleName.ADMIN);
        }

        Optional<com.example.final_project.entity.Teacher> teacherOpt = teacherRepository.findByEmail(email);
        if (teacherOpt.isPresent()) {
            com.example.final_project.entity.Teacher teacher = teacherOpt.get();
            System.out.println("DEBUG: User " + email + " found as Teacher. Assigning TEACHER role.");
            return buildUserDetails(teacher.getEmail(), teacher.getPassword(), RoleName.TEACHER);
        }

        Optional<com.example.final_project.entity.Student> studentOpt = studentRepository.findByEmail(email);
        if (studentOpt.isPresent()) {
            com.example.final_project.entity.Student student = studentOpt.get();
            System.out.println("DEBUG: User " + email + " found as Student. Assigning STUDENT role.");
            return buildUserDetails(student.getEmail(), student.getPassword(), RoleName.STUDENT);
        }

        throw new UsernameNotFoundException("Không tìm thấy người dùng: " + email);
    }

    private UserDetails buildUserDetails(String email, String hashedPassword, RoleName role) {
        String roleWithPrefix = "ROLE_" + role.name();

        Set<GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority(roleWithPrefix)
        );

        System.out.println("DEBUG: Building UserDetails for " + email + " with Authority: " + roleWithPrefix);

        return new org.springframework.security.core.userdetails.User(
                email,
                hashedPassword,
                authorities
        );
    }
}