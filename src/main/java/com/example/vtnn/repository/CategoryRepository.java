package com.example.vtnn.repository;

import com.example.vtnn.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Page<Category> findByCategoryNameContainingIgnoreCase(String categoryName, Pageable pageable);
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);
}
