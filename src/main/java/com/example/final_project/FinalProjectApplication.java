package com.example.final_project;

import com.example.final_project.entity.Admin;
import com.example.final_project.repository.AdminRepository;
import com.example.final_project.service.FileStorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;



import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.Resource;

@SpringBootApplication
public class FinalProjectApplication {

	@Resource
	FileStorageService storageService;

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().load();
		System.setProperty("GMAIL_USERNAME", dotenv.get("GMAIL_USERNAME"));
		System.setProperty("GMAIL_PASSWORD", dotenv.get("GMAIL_PASSWORD"));
		SpringApplication.run(FinalProjectApplication.class, args);
	}

	@Bean
	CommandLineRunner init() {
		return (args) -> {
			storageService.init();
		};
	}
    @Bean
    public CommandLineRunner initAdmin(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (adminRepository.findByUsername("admin").isEmpty()) {

                Admin admin = new Admin();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@gmail.com");

                adminRepository.save(admin);
                System.out.println("Tài khoản ADMIN độc lập (admin/admin123) đã được tạo thành công!");
            }
        };
    }
}

