package com.example.vtnn.controller;

import com.example.vtnn.DTO.ProductDTO;
import com.example.vtnn.model.Product;
import com.example.vtnn.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    @Autowired
    private ProductService productService;

    @PostMapping("/add")
    public Product addProduct(@RequestBody ProductDTO.ProductRequest productRequest) {
        Product product = new Product(
                productRequest.getProductName(),
                productRequest.getCategoryID(),
                productRequest.getSupplierID(),
                productRequest.getQuantityPerUnit(),
                productRequest.getUnitPrice(),
                productRequest.getUnitsInStock(),
                productRequest.getUnitsOnOrder(),
                productRequest.isDiscontinued(),
                productRequest.getProductDescription()
        );
        return productService.addProduct(product, productRequest.getImageNames());
    }

    @GetMapping
    public Page<Product> getProductsWithDetails(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "7") int size,
            @RequestParam(defaultValue = "productID,asc") String sort
    ) {
        return productService.getAllProductsWithDetails(page, size, sort);
    }

    @GetMapping("/search")
    public Page<Product> searchProduct(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "7") int size,
            @RequestParam(defaultValue = "productID,asc") String sort
    ) {
        return productService.searchProduct(keyword, page, size, sort);
    }

    @GetMapping("/all")
    public List<Product> getAllProducts() {
        return productService.getAllProduct();
    }
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable int id) {
        return productService.getProductById(id);
    }

    @GetMapping("/latest")
    public List<Product> getLatestProducts(
            @RequestParam(defaultValue = "20") int limit
    ) {
        return productService.getLatestProducts(limit);
    }
    @PutMapping("/update/{id}")
    public Product updateProduct(
            @PathVariable int id,
            @RequestBody ProductDTO.ProductRequest productRequest
    ) {
        logger.info("Received product: {}", productRequest.getProductName());
        logger.info("Image names: {}", productRequest.getImageNames());
        Product product = new Product(
                productRequest.getProductName(),
                productRequest.getCategoryID(),
                productRequest.getSupplierID(),
                productRequest.getQuantityPerUnit(),
                productRequest.getUnitPrice(),
                productRequest.getUnitsInStock(),
                productRequest.getUnitsOnOrder(),
                productRequest.isDiscontinued(),
                productRequest.getProductDescription()
        );
        return productService.updateProduct(id, product, productRequest.getImageNames());
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable int id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok("Sản phẩm và hình ảnh đã được xóa thành công");
        } catch (RuntimeException e) {
            logger.error("Lỗi khi xóa sản phẩm với ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping("/filter")
    public Page<Product> filterProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productID,asc") String sort
    ) {
        logger.info("Received keyword (raw): {}", keyword);
        String decodedKeyword = keyword != null ? URLDecoder.decode(keyword, StandardCharsets.UTF_8) : null;
        logger.info("Decoded keyword: {}", decodedKeyword);
        return productService.filterProducts(categoryId, minPrice, maxPrice, decodedKeyword, page, size, sort);
    }
}