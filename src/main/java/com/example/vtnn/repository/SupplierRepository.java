package com.example.vtnn.repository;

import com.example.vtnn.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    Optional<Supplier> findBySupplierNameIgnoreCase(String supplierName);
    Page<Supplier> findBySupplierNameContainingIgnoreCase(String keyword, Pageable pageable);
}