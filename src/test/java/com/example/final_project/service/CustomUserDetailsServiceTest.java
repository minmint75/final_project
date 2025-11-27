package com.example.final_project.service;

import com.example.final_project.entity.Teacher;
import com.example.final_project.entity.Student;
import com.example.final_project.exception.AccountLockedException;
import com.example.final_project.repository.AdminRepository;
import com.example.final_project.repository.StudentRepository;
import com.example.final_project.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class CustomUserDetailsServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private Teacher lockedTeacher;
    private Teacher activeTeacher;
    private Student lockedStudent;
    private Student activeStudent;

    @BeforeEach
    void setUp() {
        // Create locked teacher
        lockedTeacher = new Teacher();
        lockedTeacher.setTeacherId(1L);
        lockedTeacher.setEmail("locked@teacher.com");
        lockedTeacher.setPassword("password");
        lockedTeacher.setStatus(Teacher.TeacherStatus.LOCKED);

        // Create active teacher
        activeTeacher = new Teacher();
        activeTeacher.setTeacherId(2L);
        activeTeacher.setEmail("active@teacher.com");
        activeTeacher.setPassword("password");
        activeTeacher.setStatus(Teacher.TeacherStatus.APPROVED);

        // Create locked student
        lockedStudent = new Student();
        lockedStudent.setStudentId(1L);
        lockedStudent.setEmail("locked@student.com");
        lockedStudent.setPassword("password");
        lockedStudent.setStatus(Student.StudentStatus.LOCKED);

        // Create active student
        activeStudent = new Student();
        activeStudent.setStudentId(2L);
        activeStudent.setEmail("active@student.com");
        activeStudent.setPassword("password");
        activeStudent.setStatus(Student.StudentStatus.ACTIVE);
    }

    @Test
    void testLoadUserByUsername_LockedTeacher_ShouldThrowAccountLockedException() {
        // Given
        when(teacherRepository.findByEmail("locked@teacher.com")).thenReturn(Optional.of(lockedTeacher));
        when(adminRepository.findByEmail("locked@teacher.com")).thenReturn(Optional.empty());

        // When & Then
        AccountLockedException exception = assertThrows(
            AccountLockedException.class,
            () -> userDetailsService.loadUserByUsername("locked@teacher.com")
        );

        assertEquals("Tài khoản giáo viên đã bị khóa. Vui lòng liên hệ quản trị viên.", exception.getMessage());
        verify(teacherRepository, never()).save(any());
        verify(adminRepository).findByEmail("locked@teacher.com");
        verify(teacherRepository).findByEmail("locked@teacher.com");
        // studentRepository is not called due to short-circuit when teacher is found
    }

    @Test
    void testLoadUserByUsername_LockedStudent_ShouldThrowAccountLockedException() {
        // Given
        when(studentRepository.findByEmail("locked@student.com")).thenReturn(Optional.of(lockedStudent));
        when(adminRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(teacherRepository.findByEmail(any())).thenReturn(Optional.empty());

        // When & Then
        AccountLockedException exception = assertThrows(
            AccountLockedException.class,
            () -> userDetailsService.loadUserByUsername("locked@student.com")
        );

        assertEquals("Tài khoản học viên đã bị khóa. Vui lòng liên hệ quản trị viên.", exception.getMessage());
        verify(studentRepository, never()).save(any());
        verify(adminRepository).findByEmail("locked@student.com");
        verify(teacherRepository).findByEmail("locked@student.com");
    }

    @Test
    void testLoadUserByUsername_ActiveTeacher_ShouldReturnUserDetails() {
        // Given
        when(teacherRepository.findByEmail("active@teacher.com")).thenReturn(Optional.of(activeTeacher));
        when(adminRepository.findByEmail("active@teacher.com")).thenReturn(Optional.empty());

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("active@teacher.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("active@teacher.com", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER")));
        verify(teacherRepository).save(any());
        verify(adminRepository).findByEmail("active@teacher.com");
        verify(teacherRepository).findByEmail("active@teacher.com");
        // studentRepository is not called due to short-circuit when teacher is found
    }

    @Test
    void testLoadUserByUsername_ActiveStudent_ShouldReturnUserDetails() {
        // Given
        when(studentRepository.findByEmail("active@student.com")).thenReturn(Optional.of(activeStudent));
        when(adminRepository.findByEmail("active@student.com")).thenReturn(Optional.empty());
        when(teacherRepository.findByEmail("active@student.com")).thenReturn(Optional.empty());

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("active@student.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("active@student.com", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT")));
        verify(studentRepository).save(any());
        verify(adminRepository).findByEmail("active@student.com");
        verify(teacherRepository).findByEmail("active@student.com");
        verify(studentRepository).findByEmail("active@student.com");
    }

    @Test
    void testLoadUserByUsername_UserNotFound_ShouldThrowUsernameNotFoundException() {
        // Given
        when(adminRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(teacherRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(studentRepository.findByEmail(any())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("nonexistent@test.com")
        );
        
        verify(adminRepository).findByEmail("nonexistent@test.com");
        verify(teacherRepository).findByEmail("nonexistent@test.com");
        verify(studentRepository).findByEmail("nonexistent@test.com");
    }
}
