package com.example.final_project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root = Paths.get("uploads");

    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    public String save(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            Files.copy(file.getInputStream(), this.root.resolve(newFilename));
            return newFilename;
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public boolean deleteFile(String filename) {
        try {
            if (filename == null || filename.trim().isEmpty()) {
                return false;
            }
            
            Path filePath = this.root.resolve(filename);
            boolean deleted = Files.deleteIfExists(filePath);
            return deleted;
        } catch (Exception e) {
            throw new RuntimeException("Could not delete the file. Error: " + e.getMessage());
        }
    }
}
