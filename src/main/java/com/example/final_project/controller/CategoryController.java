package com.example.final_project.controller;

import com.example.final_project.dto.CategoryListDto;
import com.example.final_project.dto.CategoryRequest;
import com.example.final_project.dto.CategorySearchRequest;
import com.example.final_project.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Validated
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/search")
    public ResponseEntity<Page<CategoryListDto>> searchCategories(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        CategorySearchRequest request = new CategorySearchRequest();
        request.setName(name);
        request.setPage(page);
        request.setSize(size);
        if (sort != null && sort.contains(",")) {
            String[] sortParts = sort.split(",");
            request.setSort(sortParts[0]);
            request.setDirection(sortParts[1]);
        }

        try {
            Page<CategoryListDto> result = categoryService.searchCategories(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Consider more specific exception handling
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request parameters", e);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<CategoryListDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<CategoryListDto> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<CategoryListDto> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        try {
            CategoryListDto created = categoryService.saveCategory(categoryRequest);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        }
    }

    @PatchMapping("/edit/{id}")
    public ResponseEntity<CategoryListDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest categoryRequest) {
        
        categoryRequest.setId(id); // Ensure ID from path is used
        
        try {
            CategoryListDto updated = categoryService.saveCategory(categoryRequest);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategoryById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            // This could be 404 Not Found or 400 Bad Request depending on context
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        }
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }
}
