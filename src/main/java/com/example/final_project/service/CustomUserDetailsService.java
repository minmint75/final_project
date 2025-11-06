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
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        // Try Admin by username only
        return adminRepository.findByUsername(usernameOrEmail)
                .map(admin -> buildUserDetails(admin.getUsername(), admin.getPassword(), admin.getRoleName()))
                .orElseGet(() ->
                        // Try Teacher by username or email
                        teacherRepository.findByUsername(usernameOrEmail)
                                .or(() -> teacherRepository.findByEmail(usernameOrEmail))
                                .map(teacher -> buildUserDetails(teacher.getUsername(), teacher.getPassword(), teacher.getRoleName()))
                                .orElseGet(() ->
                                        // Try Student by username or email
                                        studentRepository.findByUsername(usernameOrEmail)
                                                .or(() -> studentRepository.findByEmail(usernameOrEmail))
                                                .map(student -> buildUserDetails(student.getUsername(), student.getPassword(), student.getRoleName()))
                                                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + usernameOrEmail))
                                )
                );
    }

    private UserDetails buildUserDetails(String username, String hashedPassword, RoleName role) {
        String roleWithPrefix = "ROLE_" + role.name();

        Set<GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority(roleWithPrefix)
        );

        return new org.springframework.security.core.userdetails.User(
                username,
                hashedPassword,
                authorities
        );
    }
}