package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.CategorySearchRequest;
import com.example.final_project.entity.Category;
import com.example.final_project.entity.RoleName;
// import com.example.final_project.repository.QuestionRepository; // Temporarily commented out
import com.example.final_project.repository.CategoryRepository;
import com.example.final_project.service.CategoryService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // TODO: Uncomment and implement QuestionRepository when Question entity is available
    // @Autowired
    // private QuestionRepository questionRepository;

    @Override
    public Page<Category> findAll(CategorySearchRequest request) {
        Sort sort = Sort.by(request.getSortDirection(), request.getSort());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<Category> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(request.getName())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + request.getName().toLowerCase() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return categoryRepository.findAll(spec, pageable);
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }

    @Override
    public Category save(Category category) {
        // Backlog: Capitalize first letter
        if (StringUtils.hasText(category.getName())) {
            String capitalizedName = StringUtils.capitalize(category.getName().trim());
            category.setName(capitalizedName);
        }


        if (category.getId() == null) { // Creating a new category
            categoryRepository.findByName(category.getName()).ifPresent(c -> {
                throw new IllegalArgumentException("Category name already exists");
            });
            // Set creator
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            category.setCreatedBy(authentication.getName());
        } else { // Updating an existing category
            categoryRepository.findByNameAndIdNot(category.getName(), category.getId()).ifPresent(c -> {
                throw new IllegalArgumentException("Category name already exists");
            });
            // Backlog: Teacher can only update their own categories
            Category existingCategory = categoryRepository.findById(category.getId())
                    .orElseThrow(() -> new IllegalStateException("Category not found"));
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + RoleName.ADMIN.name()));
            if (!isAdmin && !existingCategory.getCreatedBy().equals(authentication.getName())) {
                throw new SecurityException("You are not allowed to update this category");
            }
            category.setCreatedBy(existingCategory.getCreatedBy()); // Preserve original creator
        }

        return categoryRepository.save(category);
    }

    @Override
    public void deleteById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Category with id " + id + " not found"));

        // Backlog: Authorization check for deletion
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + RoleName.ADMIN.name()));
        if (!isAdmin && !category.getCreatedBy().equals(authentication.getName())) {
            throw new SecurityException("You are not allowed to delete this category");
        }

        // TODO: Uncomment and implement this section when Question entity and QuestionRepository are available
        // Backlog: Only delete if category has no questions
        // long questionCount = questionRepository.countByCategoryId(id);
        // if (questionCount > 0) {
        //     throw new IllegalStateException("Cannot delete category with existing questions");
        // }

        categoryRepository.deleteById(id);
    }
}