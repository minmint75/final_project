package com.example.final_project.service.serviceImpl;

import com.example.final_project.dto.CategoryListDto;
import com.example.final_project.dto.CategoryRequest;
import com.example.final_project.dto.CategorySearchRequest;
import com.example.final_project.entity.Category;
import com.example.final_project.entity.RoleName;
import com.example.final_project.repository.AdminRepository;
import com.example.final_project.repository.CategoryRepository;
import com.example.final_project.repository.TeacherRepository;
import com.example.final_project.service.CategoryService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final AdminRepository adminRepository;
    private final TeacherRepository teacherRepository;

    private CategoryListDto toCategoryListDto(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryListDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                resolveRole(category.getCreatedBy()),
                resolveName(category.getCreatedBy())
        );
    }

    @Override
    public Page<CategoryListDto> searchCategories(CategorySearchRequest request) {
        Sort sort = Sort.by(request.isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC, request.getSort());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<Category> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.hasFilters()) {
                if (StringUtils.hasText(request.getName())) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + request.getName().toLowerCase() + "%"));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return categoryRepository.findAll(spec, pageable).map(this::toCategoryListDto);
    }

    @Override
    public List<CategoryListDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toCategoryListDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CategoryListDto> getCategoryById(Long id) {
        return categoryRepository.findById(id).map(this::toCategoryListDto);
    }

    @Override
    public CategoryListDto saveCategory(CategoryRequest categoryRequest) {
        String capitalizedName = StringUtils.capitalize(categoryRequest.getName().trim());
        categoryRequest.setName(capitalizedName);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Category category;
        if (categoryRequest.getId() == null) { // Create
            categoryRepository.findByName(categoryRequest.getName()).ifPresent(c -> {
                throw new IllegalArgumentException("Tên danh mục đã tồn tại");
            });
            category = new Category();
            category.setCreatedBy(username);
        } else { // Update
            category = categoryRepository.findById(categoryRequest.getId())
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy danh mục"));
            categoryRepository.findByNameAndIdNot(categoryRequest.getName(), categoryRequest.getId()).ifPresent(c -> {
                throw new IllegalArgumentException("Tên danh mục đã tồn tại");
            });

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + RoleName.ADMIN.name()));
            boolean isOwner = category.getCreatedBy().equals(username);

            if (!isAdmin && !isOwner) {
                throw new SecurityException("Bạn không có quyền sửa danh mục này");
            }
        }

        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        category.setCreatedByRole(resolveRole(category.getCreatedBy()));
        category.setCreatedByName(resolveName(category.getCreatedBy()));

        Category savedCategory = categoryRepository.save(category);
        return toCategoryListDto(savedCategory);
    }

    @Override
    public void deleteCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy danh mục với ID: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + RoleName.ADMIN.name()));
        boolean isOwner = category.getCreatedBy().equals(authentication.getName());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Bạn không có quyền xóa danh mục này");
        }

        if(!category.getQuestions().isEmpty() || !category.getExam().isEmpty()){
            throw new IllegalStateException("Không thể xóa danh mục đã chứa câu hỏi hoặc bài thi.");
        }

        categoryRepository.deleteById(id);
    }

    private String resolveRole(String email) {
        if (email == null || email.isBlank()) return null;
        if (teacherRepository.findByEmail(email).isPresent()) return "teacher";
        if (adminRepository.findByEmail(email).isPresent()) return "admin";
        return null;
    }

    private String resolveName(String email) {
        if (email == null || email.isBlank()) return null;
        var teacherOpt = teacherRepository.findByEmail(email);
        if (teacherOpt.isPresent()) {
            return teacherOpt.get().getUsername();
        }
        var adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            return adminOpt.get().getUsername();
        }
        return "unknown";
    }
}
