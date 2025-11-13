package com.example.final_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.example.final_project.service.StudentService;
import com.example.final_project.service.TeacherService;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TeacherService teacherService;
    private final StudentService studentService;

    public SecurityConfig(TeacherService teacherService, StudentService studentService) {
        this.teacherService = teacherService;
        this.studentService = studentService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .authorizeHttpRequests(authz -> authz
                .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/logout").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/test").permitAll()
                .requestMatchers("/api/register/**", "/api/forgot-password", "/api/reset-password", "/api/validate-token").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/change-password").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/student/**").hasRole("STUDENT")
                .requestMatchers("/teacher/**").hasRole("TEACHER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginProcessingUrl("/api/login")
                .usernameParameter("email") // from the frontend code, it uses 'email'
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
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
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
            
            // Get user details
            Object principal = authentication.getPrincipal();
            String email = "";
            String role = "";
            
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                email = userDetails.getUsername();
                role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse("");
                
                // Update last visit for both teachers and students
                try {
                    if (role.equals("ROLE_TEACHER")) {
                        teacherService.updateLastVisit(email);
                    } else if (role.equals("ROLE_STUDENT")) {
                        studentService.updateLastVisit(email);
                    }
                } catch (Exception e) {
                    // Log the error but don't fail the login
                    System.err.println("Error updating last visit for " + role + ": " + e.getMessage());
                }
            }
            
            // Prepare response
            String roleName = role.replace("ROLE_", "");
            String jsonResponse = String.format("{\"message\": \"Login successful\", \"role\": \"%s\"}", roleName);
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