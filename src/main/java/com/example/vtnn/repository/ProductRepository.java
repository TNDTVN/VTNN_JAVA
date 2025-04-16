package com.example.vtnn.repository;

import com.example.vtnn.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    List<Product> findByProductNameContainingIgnoreCase(String productName);
    @EntityGraph(attributePaths = {"category", "supplier", "images"})
    @Query("SELECT DISTINCT p FROM Product p")
    Page<Product> findAllWithDetails(Pageable pageable);
    Page<Product> findByProductNameContainingIgnoreCase(String keyword, Pageable pageable);
    long countByCategoryID(int categoryID);
    long countBySupplierID(int supplierID);
}
