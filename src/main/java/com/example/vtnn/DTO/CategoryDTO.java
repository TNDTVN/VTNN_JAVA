package com.example.vtnn.DTO;

import com.example.vtnn.model.Category;

public class CategoryDTO {
    public static class CategoryRequestDTO {
        private String categoryName;
        private String description;

        // Constructor mặc định (yêu cầu bởi Jackson)
        public CategoryRequestDTO() {}

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // DTO cho response
    public static class CategoryResponseDTO {
        private int categoryID;
        private String categoryName;
        private String description;

        // Constructor mặc định
        public CategoryResponseDTO() {}

        public CategoryResponseDTO(Category category) {
            this.categoryID = category.getCategoryID();
            this.categoryName = category.getCategoryName();
            this.description = category.getDescription();
        }

        public int getCategoryID() { return categoryID; }
        public void setCategoryID(int categoryID) { this.categoryID = categoryID; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
