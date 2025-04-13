package com.example.vtnn.controller;

import com.example.vtnn.model.Supplier;
import com.example.vtnn.service.SupplierService;
import com.example.vtnn.service.SupplierService.SupplierRequestDTO;
import com.example.vtnn.service.SupplierService.SupplierResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;

    @PostMapping
    public ResponseEntity<?> addSupplier(@RequestBody SupplierRequestDTO supplierDTO) {
        try {
            Supplier savedSupplier = supplierService.addSupplier(supplierDTO);
            return new ResponseEntity<>(savedSupplier, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSupplier(@PathVariable int id) {
        try {
            supplierService.deleteSupplier(id);
            return new ResponseEntity<>("Xóa nhà cung cấp thành công", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable int id) {
        try {
            Supplier supplier = supplierService.getSupplierById(id);
            return ResponseEntity.ok(supplier);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<Page<SupplierResponseDTO>> getSuppliersByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "supplierID,asc") String sort
    ) {
        Page<SupplierResponseDTO> supplierDTOS = supplierService.getAllSuppliersInPage(page, size, sort);
        return ResponseEntity.ok(supplierDTOS);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SupplierResponseDTO>> searchSuppliers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "supplierID,asc") String sort
    ) {
        Page<SupplierResponseDTO> suppliers = supplierService.searchSuppliers(keyword, page, size, sort);
        return ResponseEntity.ok(suppliers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSupplier(@PathVariable int id, @RequestBody SupplierRequestDTO supplierDTO) {
        try {
            Supplier supplier = supplierService.getSupplierById(id);
            supplier.setSupplierName(supplierDTO.getSupplierName().trim());
            supplier.setContactName(supplierDTO.getContactName() != null ? supplierDTO.getContactName().trim() : null);
            supplier.setAddress(supplierDTO.getAddress() != null ? supplierDTO.getAddress().trim() : null);
            supplier.setCity(supplierDTO.getCity() != null ? supplierDTO.getCity().trim() : null);
            supplier.setPostalCode(supplierDTO.getPostalCode() != null ? supplierDTO.getPostalCode().trim() : null);
            supplier.setCountry(supplierDTO.getCountry() != null ? supplierDTO.getCountry().trim() : null);
            supplier.setPhone(supplierDTO.getPhone() != null ? supplierDTO.getPhone().trim() : null);
            supplier.setEmail(supplierDTO.getEmail() != null ? supplierDTO.getEmail().trim() : null);
            Supplier updatedSupplier = supplierService.saveSupplier(supplier);
            return new ResponseEntity<>(updatedSupplier, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}