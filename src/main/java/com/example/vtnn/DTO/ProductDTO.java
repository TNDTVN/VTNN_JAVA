package com.example.vtnn.DTO;

import java.math.BigDecimal;
import java.util.List;

public class ProductDTO {
    public static class ProductRequest {
        private String productName;
        private int categoryID;
        private int supplierID;
        private String quantityPerUnit;
        private BigDecimal unitPrice;
        private int unitsInStock;
        private int unitsOnOrder;
        private boolean discontinued;
        private String productDescription;
        private List<String> imageNames;

        // Getters, setters...
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getCategoryID() { return categoryID; }
        public void setCategoryID(int categoryID) { this.categoryID = categoryID; }
        public int getSupplierID() { return supplierID; }
        public void setSupplierID(int supplierID) { this.supplierID = supplierID; }
        public String getQuantityPerUnit() { return quantityPerUnit; }
        public void setQuantityPerUnit(String quantityPerUnit) { this.quantityPerUnit = quantityPerUnit; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public int getUnitsInStock() { return unitsInStock; }
        public void setUnitsInStock(int unitsInStock) { this.unitsInStock = unitsInStock; }
        public int getUnitsOnOrder() { return unitsOnOrder; }
        public void setUnitsOnOrder(int unitsOnOrder) { this.unitsOnOrder = unitsOnOrder; }
        public boolean isDiscontinued() { return discontinued; }
        public void setDiscontinued(boolean discontinued) { this.discontinued = discontinued; }
        public String getProductDescription() { return productDescription; }
        public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
        public List<String> getImageNames() { return imageNames; }
        public void setImageNames(List<String> imageNames) { this.imageNames = imageNames; }
    }
}
