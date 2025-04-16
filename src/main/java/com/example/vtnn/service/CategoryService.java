package com.example.vtnn.service;

import com.example.vtnn.model.Category;
import com.example.vtnn.repository.CategoryRepository;
import com.example.vtnn.repository.ProductRepository;
import com.example.vtnn.DTO.CategoryDTO.CategoryResponseDTO;
import com.example.vtnn.DTO.CategoryDTO.CategoryRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public Category addCategory(CategoryRequestDTO categoryDTO) {
        if (categoryDTO == null) {
            throw new IllegalArgumentException("Thông tin loại sản phẩm không được null");
        }
        if (categoryDTO.getCategoryName() == null || categoryDTO.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại sản phẩm không được để trống");
        }

        Optional<Category> existingCategory = categoryRepository.findByCategoryNameIgnoreCase(categoryDTO.getCategoryName().trim());
        if (existingCategory.isPresent()) {
            throw new RuntimeException("Loại sản phẩm '" + categoryDTO.getCategoryName() + "' đã tồn tại");
        }

        Category category = new Category();
        category.setCategoryName(categoryDTO.getCategoryName().trim());
        if (categoryDTO.getDescription() != null) {
            category.setDescription(categoryDTO.getDescription().trim());
        }

        try {
            return categoryRepository.save(category);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu loại sản phẩm: " + e.getMessage(), e);
        }
    }

    public void deleteCategory(int id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại sản phẩm với ID: " + id));

        long productCount = productRepository.countByCategoryID(id);
        if (productCount > 0) {
            throw new RuntimeException("Không thể xóa loại sản phẩm '" + category.getCategoryName() + "' vì đang có " + productCount + " sản phẩm liên quan");
        }

        try {
            categoryRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa loại sản phẩm: " + e.getMessage(), e);
        }
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Page<CategoryResponseDTO> getAllcategoriesInPage(int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortField));
        return categoryRepository.findAll(pageable).map(CategoryResponseDTO::new);
    }

    public Category getCategoryById(int id) {
        return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy loại sản phẩm với ID: " + id));
    }

    public Page<CategoryResponseDTO> searchCategories(String keyword, int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortField));
        Page<Category> categoryPage = categoryRepository.findByCategoryNameContainingIgnoreCase(keyword, pageable);
        return categoryPage.map(CategoryResponseDTO::new);
    }
    public Category saveCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Thông tin loại sản phẩm không được null");
        }
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại sản phẩm không được để trống");
        }

        Optional<Category> existingCategory = categoryRepository.findByCategoryNameIgnoreCase(category.getCategoryName().trim());
        if (existingCategory.isPresent() && existingCategory.get().getCategoryID() != category.getCategoryID()) {
            throw new RuntimeException("Loại sản phẩm '" + category.getCategoryName() + "' đã tồn tại");
        }

        try {
            return categoryRepository.save(category);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu loại sản phẩm: " + e.getMessage(), e);
        }
    }
}