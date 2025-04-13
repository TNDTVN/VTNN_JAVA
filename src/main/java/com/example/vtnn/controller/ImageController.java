package com.example.vtnn.controller;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/images")
public class ImageController {
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    @Value("${upload.path}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        File directory = new File(uploadPath);
        if (!directory.isAbsolute()) {
            uploadPath = new File(System.getProperty("user.dir"), uploadPath).getAbsolutePath();
        }
        System.out.println("Upload Path: " + uploadPath);
    }

    // Endpoint upload nhiều ảnh (giữ nguyên cho product)
    @PostMapping("/upload-multiple")
    public ResponseEntity<?> uploadMultipleImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body("No files uploaded");
        }

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            try {
                Files.createDirectories(Paths.get(uploadPath));
            } catch (IOException e) {
                logger.error("Failed to create upload directory: {}", e.getMessage(), e);
                return ResponseEntity.status(500).body("Failed to create upload directory: " + e.getMessage());
            }
        }

        List<String> uploadedFileNames = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                try {
                    String originalFileName = file.getOriginalFilename();
                    if (originalFileName == null) continue;

                    String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(originalFileName);
                    File destinationFile = new File(uploadPath + File.separator + fileName);
                    file.transferTo(destinationFile);
                    uploadedFileNames.add(fileName);
                } catch (IOException e) {
                    logger.error("Error uploading file '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
                    return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
                }
            }
        }

        return uploadedFileNames.isEmpty()
                ? ResponseEntity.badRequest().body("No valid files were uploaded")
                : ResponseEntity.ok(uploadedFileNames);
    }

    // Thêm endpoint mới để upload một ảnh duy nhất cho account
    @PostMapping("/upload-single")
    public ResponseEntity<?> uploadSingleImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded");
        }

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            try {
                Files.createDirectories(Paths.get(uploadPath));
            } catch (IOException e) {
                logger.error("Failed to create upload directory: {}", e.getMessage(), e);
                return ResponseEntity.status(500).body("Failed to create upload directory: " + e.getMessage());
            }
        }

        try {
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null) {
                return ResponseEntity.badRequest().body("Invalid file name");
            }

            String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(originalFileName);
            File destinationFile = new File(uploadPath + File.separator + fileName);
            file.transferTo(destinationFile);
            logger.info("Uploaded single file: {}", fileName);
            return ResponseEntity.ok(fileName); // Trả về tên file duy nhất
        } catch (IOException e) {
            logger.error("Error uploading file '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
            return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
        }
    }

    // Endpoint lấy ảnh (giữ nguyên)
    @GetMapping("/{imageName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageName) {

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            try {
                Files.createDirectories(Paths.get(uploadPath));
            } catch (IOException e) {
                logger.error("Failed to create upload directory: {}", e.getMessage(), e);
                return ResponseEntity.status(500).build();
            }
        }

        try {
            File file = new File(uploadPath + File.separator + imageName);
            if (!file.exists()) {
                logger.warn("Image not found: {}", imageName);
                return ResponseEntity.notFound().build();
            }

            byte[] imageBytes = Files.readAllBytes(file.toPath());
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageBytes);
        } catch (IOException e) {
            logger.error("Error reading image '{}': {}", imageName, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}