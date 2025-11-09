package com.example.final_project.service;

import com.example.final_project.dto.CategorySearchRequest;
import com.example.final_project.entity.Category;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    Page<Category> findAll(CategorySearchRequest request);

    List<Category> findAll();

    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    Category save(Category category);

    void deleteById(Long id);
}