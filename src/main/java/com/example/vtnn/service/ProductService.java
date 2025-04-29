package com.example.vtnn.service;

import com.example.vtnn.model.Image;
import com.example.vtnn.model.Product;
import com.example.vtnn.repository.ImageRepository;
import com.example.vtnn.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getLatestProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "productID")); // Sắp xếp theo productID giảm dần
        return productRepository.findAllWithDetails(pageable).getContent();
    }

    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    public Product getProductById(int id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }


    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private ImageService imageService;
    @Transactional
    public Product updateProduct(int id, Product updatedProduct, List<String> newImageNames) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        existingProduct.setProductName(updatedProduct.getProductName());
        existingProduct.setProductDescription(updatedProduct.getProductDescription());
        existingProduct.setUnitPrice(updatedProduct.getUnitPrice());
        existingProduct.setCategoryID(updatedProduct.getCategoryID());
        existingProduct.setSupplierID(updatedProduct.getSupplierID());
        existingProduct.setQuantityPerUnit(updatedProduct.getQuantityPerUnit());
        existingProduct.setUnitsInStock(updatedProduct.getUnitsInStock());
        existingProduct.setUnitsOnOrder(updatedProduct.getUnitsOnOrder());
        existingProduct.setDiscontinued(updatedProduct.isDiscontinued());

        List<Image> existingImages = imageRepository.findAllByProductID(id);
        List<String> existingImageNames = existingImages.stream()
                .map(Image::getImageName)
                .collect(Collectors.toList());

        logger.info("Existing images: {}", existingImageNames);
        logger.info("New image names: {}", newImageNames);

        if (newImageNames != null && !newImageNames.isEmpty()) {
            if (!newImageNames.equals(existingImageNames)) {
                for (Image image : existingImages) {
                    logger.info("Deleting image file: {}", image.getImageName());
                    imageService.deleteImage(image.getImageName());
                }
                logger.info("Deleting all image records for productID: {}", id);
                imageRepository.deleteAllByProductID(id);
                imageRepository.flush();

                for (String imageName : newImageNames) {
                    logger.info("Saving new image: {}", imageName);
                    Image newImage = new Image(existingProduct.getProductID(), imageName);
                    imageRepository.save(newImage);
                }
            }
        }

        return productRepository.save(existingProduct);
    }
    public void deleteProduct(int id) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<Image> existingImages = imageRepository.findAllByProductID(id);
        for (Image image : existingImages) {
            imageService.deleteImage(image.getImageName());
            imageRepository.delete(image);
        }

        productRepository.delete(existingProduct);
    }

    public Page<Product> getAllProductsWithDetails(int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortField));
        return productRepository.findAllWithDetails(pageable);
    }

    public Page<Product> searchProduct(String keyword, int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortField));
        return productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);
    }

    public Product addProduct(Product product, List<String> imageNames) {
        Product savedProduct = productRepository.save(product);

        if (imageNames != null && !imageNames.isEmpty()) {
            for (String imageName : imageNames) {
                Image image = new Image(savedProduct.getProductID(), imageName);
                imageRepository.save(image);
            }
        }

        return savedProduct;
    }
    public Page<Product> filterProducts(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String keyword, int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Sort sortObj = Sort.by(sortDirection, sortField);

        Pageable pageable = PageRequest.of(page - 1, size, sortObj);

        Specification<Product> spec = Specification.where(null);

        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoryID"), categoryId));
        }
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("unitPrice"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("unitPrice"), maxPrice));
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Sử dụng từ khóa gốc, không chuẩn hóa
            String searchKeyword = keyword.toLowerCase();
            logger.info("Search keyword: {}", searchKeyword);
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.function("LOWER", String.class, root.get("productName")),
                            "%" + searchKeyword + "%"
                    )
            );
        }

        Page<Product> result = productRepository.findAll(spec, pageable);
        logger.info("Found {} products for keyword: {}", result.getTotalElements(), keyword);
        return result;
    }
}
