package com.example.final_project.dto;

import com.example.final_project.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    private Long id; // dùng khi update, null khi tạo mới

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String description;
    
    private String createdBy; // Email of the creator (Admin or Teacher)

    public static CategoryRequest fromEntity(Category category) {
        if (category == null) {
            return null;
        }
        CategoryRequest req = new CategoryRequest();
        req.setId(category.getId());
        req.setName(category.getName());
        req.setDescription(category.getDescription());
        req.setCreatedBy(category.getCreatedBy());
        return req;
    }

    public Category toEntity() {
        Category category = new Category();
        category.setId(this.id);
        category.setName(this.name);
        category.setDescription(this.description);
        category.setCreatedBy(this.createdBy);
        return category;
    }

}
