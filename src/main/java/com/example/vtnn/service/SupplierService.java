package com.example.vtnn.service;

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

    public Supplier addSupplier(SupplierRequestDTO supplierDTO) {
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

    public Page<SupplierResponseDTO> getAllSuppliersInPage(int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortField));
        return supplierRepository.findAll(pageable).map(SupplierResponseDTO::new);
    }

    public Supplier getSupplierById(int id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp với ID: " + id));
    }

    public Page<SupplierResponseDTO> searchSuppliers(String keyword, int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortField));
        Page<Supplier> supplierPage = supplierRepository.findBySupplierNameContainingIgnoreCase(keyword, pageable);
        return supplierPage.map(SupplierResponseDTO::new);
    }

    // DTO cho request
    public static class SupplierRequestDTO {
        private String supplierName;
        private String contactName;
        private String address;
        private String city;
        private String postalCode;
        private String country;
        private String phone;
        private String email;

        public SupplierRequestDTO() {}

        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
        public String getContactName() { return contactName; }
        public void setContactName(String contactName) { this.contactName = contactName; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // DTO cho response
    public static class SupplierResponseDTO {
        private int supplierID;
        private String supplierName;
        private String contactName;
        private String address;
        private String city;
        private String postalCode;
        private String country;
        private String phone;
        private String email;

        public SupplierResponseDTO() {}

        public SupplierResponseDTO(Supplier supplier) {
            this.supplierID = supplier.getSupplierID();
            this.supplierName = supplier.getSupplierName();
            this.contactName = supplier.getContactName();
            this.address = supplier.getAddress();
            this.city = supplier.getCity();
            this.postalCode = supplier.getPostalCode();
            this.country = supplier.getCountry();
            this.phone = supplier.getPhone();
            this.email = supplier.getEmail();
        }

        public int getSupplierID() { return supplierID; }
        public void setSupplierID(int supplierID) { this.supplierID = supplierID; }
        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
        public String getContactName() { return contactName; }
        public void setContactName(String contactName) { this.contactName = contactName; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}