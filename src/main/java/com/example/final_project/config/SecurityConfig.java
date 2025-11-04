package com.example.final_project.config;

import com.example.final_project.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http


                // Cấu hình Authorization
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/", "/css/**", "/js/**").permitAll() // Cho phép truy cập công khai

                        .requestMatchers("/admin/**").hasRole("ADMIN")                 // Chỉ ADMIN truy cập khu vực /admin

                        .requestMatchers("/teacher/**").hasAnyRole("ADMIN", "GIẢNG_VIÊN") // ADMIN và GIẢNG_VIÊN truy cập /teacher

                        .requestMatchers("/student/**").hasRole("HỌC_VIÊN")            // Chỉ HỌC_VIÊN truy cập khu vực /student

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureUrl("/login?error")
                        .permitAll()
                )

                // Cấu hình Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new RoleBasedSuccessHandler();
    }
}
