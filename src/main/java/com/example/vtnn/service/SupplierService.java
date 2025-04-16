package com.example.vtnn.service;

import com.example.vtnn.DTO.SupplierDTO;
import com.example.vtnn.model.Supplier;
import com.example.vtnn.repository.ProductRepository;
import com.example.vtnn.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    public Supplier addSupplier(SupplierDTO.SupplierRequestDTO supplierDTO) {
        if (supplierDTO == null) {
            throw new IllegalArgumentException("Thông tin nhà cung cấp không được null");
        }
        if (supplierDTO.getSupplierName() == null || supplierDTO.getSupplierName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống");
        }

        Optional<Supplier> existingSupplier = supplierRepository.findBySupplierNameIgnoreCase(supplierDTO.getSupplierName().trim());
        if (existingSupplier.isPresent()) {
            throw new RuntimeException("Nhà cung cấp '" + supplierDTO.getSupplierName() + "' đã tồn tại");
        }

        Supplier supplier = new Supplier();
        supplier.setSupplierName(supplierDTO.getSupplierName().trim());
        supplier.setContactName(supplierDTO.getContactName() != null ? supplierDTO.getContactName().trim() : null);
        supplier.setAddress(supplierDTO.getAddress() != null ? supplierDTO.getAddress().trim() : null);
        supplier.setCity(supplierDTO.getCity() != null ? supplierDTO.getCity().trim() : null);
        supplier.setPostalCode(supplierDTO.getPostalCode() != null ? supplierDTO.getPostalCode().trim() : null);
        supplier.setCountry(supplierDTO.getCountry() != null ? supplierDTO.getCountry().trim() : null);
        supplier.setPhone(supplierDTO.getPhone() != null ? supplierDTO.getPhone().trim() : null);
        supplier.setEmail(supplierDTO.getEmail() != null ? supplierDTO.getEmail().trim() : null);

        try {
            return supplierRepository.save(supplier);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu nhà cung cấp: " + e.getMessage(), e);
        }
    }

    public void deleteSupplier(int id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp với ID: " + id));

        long productCount = productRepository.countBySupplierID(id);
        if (productCount > 0) {
            throw new RuntimeException("Không thể xóa nhà cung cấp '" + supplier.getSupplierName() + "' vì đang có " + productCount + " sản phẩm liên quan");
        }

        try {
            supplierRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa nhà cung cấp: " + e.getMessage(), e);
        }
    }
    public Supplier saveSupplier(Supplier supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("Thông tin nhà cung cấp không được null");
        }
        if (supplier.getSupplierName() == null || supplier.getSupplierName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống");
        }

        Optional<Supplier> existingSupplier = supplierRepository.findBySupplierNameIgnoreCase(supplier.getSupplierName().trim());
        if (existingSupplier.isPresent() && existingSupplier.get().getSupplierID() != supplier.getSupplierID()) {
            throw new RuntimeException("Nhà cung cấp '" + supplier.getSupplierName() + "' đã tồn tại");
        }

        try {
            return supplierRepository.save(supplier);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu nhà cung cấp: " + e.getMessage(), e);
        }
    }
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Page<SupplierDTO.SupplierResponseDTO> getAllSuppliersInPage(int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortField));
        return supplierRepository.findAll(pageable).map(SupplierDTO.SupplierResponseDTO::new);
    }

    public Supplier getSupplierById(int id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp với ID: " + id));
    }

    public Page<SupplierDTO.SupplierResponseDTO> searchSuppliers(String keyword, int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortField));
        Page<Supplier> supplierPage = supplierRepository.findBySupplierNameContainingIgnoreCase(keyword, pageable);
        return supplierPage.map(SupplierDTO.SupplierResponseDTO::new);
    }

}