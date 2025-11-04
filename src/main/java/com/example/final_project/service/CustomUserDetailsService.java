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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return adminRepository.findByUsername(username)
                .map(admin -> buildUserDetails(admin.getUsername(), admin.getPassword(), admin.getRoleName()))
                .orElseGet(() ->
                        teacherRepository.findByUsername(username)
                                .map(teacher -> buildUserDetails(teacher.getUsername(), teacher.getPassword(), teacher.getRoleName()))
                                .orElseGet(() ->
                                        studentRepository.findByUsername(username)
                                                .map(student -> buildUserDetails(student.getUsername(), student.getPassword(), student.getRoleName()))
                                                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username))
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