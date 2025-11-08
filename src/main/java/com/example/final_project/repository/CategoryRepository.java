package com.example.final_project.repository;

import com.example.final_project.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    Optional<Category> findByNameAndIdNot(String name, Long id);

    Page<Category> findByCreatedBy(String createdBy, Pageable pageable);
}
