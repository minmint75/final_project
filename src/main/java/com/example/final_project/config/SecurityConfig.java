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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disabling CSRF for simplicity with SPA. Re-evaluate if using session cookies.
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(HttpMethod.POST, "/api/perform_login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/perform_logout").permitAll()
                .requestMatchers("/api/register/**", "/api/forgot-password", "/api/reset-password", "/api/validate-token").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/student/**").hasRole("STUDENT")
                .requestMatchers("/teacher/**").hasRole("TEACHER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginProcessingUrl("/api/perform_login")
                .usernameParameter("email") // from the frontend code, it uses 'email'
                .passwordParameter("password")
                .successHandler(successHandler())
                .failureHandler(failureHandler())
            )
            .logout(logout -> logout
                .logoutUrl("/api/perform_logout")
                .logoutSuccessHandler(logoutSuccessHandler())
            );

        return http.build();
    }

    private AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            response.setStatus(200);
            response.setContentType("application/json");
            String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("");
            
            // Assuming roles are like "ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN"
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