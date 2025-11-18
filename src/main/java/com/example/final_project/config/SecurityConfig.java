package com.example.final_project.config;

import com.example.final_project.repository.AdminRepository;
import com.example.final_project.repository.StudentRepository;
import com.example.final_project.repository.TeacherRepository;
import com.example.final_project.service.StudentService;
import com.example.final_project.service.TeacherService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TeacherService teacherService;
    private final StudentService studentService;
    private final AdminRepository adminRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public SecurityConfig(TeacherService teacherService, StudentService studentService, AdminRepository adminRepository, TeacherRepository teacherRepository, StudentRepository studentRepository) {
        this.teacherService = teacherService;
        this.studentService = studentService;
        this.adminRepository = adminRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers("/api/register", "/api/forgot-password", "/api/reset-password", "/api/validate-token")
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.POST, "/api/login", "/api/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/test", "/uploads/**").permitAll()
                        .requestMatchers("/api/register/**", "/api/forgot-password", "/api/reset-password", "/api/validate-token").permitAll()

                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/teacher/**").hasAuthority("ROLE_TEACHER")
                        .requestMatchers("/api/student/**").hasAuthority("ROLE_STUDENT")

                        .requestMatchers("/api/me", "/api/change-password").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(successHandler())
                        .failureHandler(failureHandler())
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized. Please log in.\"}");
                        })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers","X-XSRF-TOKEN"));
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            response.setStatus(200);
            response.setContentType("application/json");

            Object principal = authentication.getPrincipal();
            String email = "";
            String role = "";
            final Map<String, String> userDetailsMap = new HashMap<>();

            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                email = userDetails.getUsername();

                role = userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse("ROLE_UNKNOWN");

                if (role.equals("ROLE_ADMIN")) {
                    adminRepository.findByEmail(email).ifPresent(admin -> {
                        userDetailsMap.put("id", String.valueOf(admin.getId()));
                        userDetailsMap.put("name", admin.getUsername());
                    });
                } else if (role.equals("ROLE_TEACHER")) {
                    teacherRepository.findByEmail(email).ifPresent(teacher -> {
                        userDetailsMap.put("id", String.valueOf(teacher.getTeacherId()));
                        userDetailsMap.put("name", teacher.getUsername());
                    });
                } else if (role.equals("ROLE_STUDENT")) {
                    studentRepository.findByEmail(email).ifPresent(student -> {
                        userDetailsMap.put("id", String.valueOf(student.getStudentId()));
                        userDetailsMap.put("name", student.getUsername());
                    });
                }

                // Update last visit
                try {
                    if (role.equals("ROLE_TEACHER")) {
                        teacherService.updateLastVisit(email);
                    } else if (role.equals("ROLE_STUDENT")) {
                        studentService.updateLastVisit(email);
                    }
                } catch (Exception e) {
                    System.err.println("Error updating last visit for " + role + ": " + e.getMessage());
                }
            }

            String id = userDetailsMap.getOrDefault("id", "");
            String name = userDetailsMap.getOrDefault("name", "");
            // Bỏ tiền tố ROLE_ để client nhận được chuỗi "ADMIN"
            String roleName = role.replace("ROLE_", "");
            String jsonResponse = String.format("{\"message\": \"Login successful\", \"id\": \"%s\", \"email\": \"%s\", \"name\": \"%s\", \"role\": \"%s\"}", id, email, name, roleName);

            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
        };
    }

    private AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            response.setStatus(401);
            response.getWriter().write("{\"error\": \"Invalid credentials\"}");
            response.getWriter().flush();
        };
    }

    private LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(200);
            response.getWriter().write("{\"message\": \"Logout successful\"}");
            response.getWriter().flush();
        };
    }
}