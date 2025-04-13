package com.example.vtnn.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;

@Service
public class ImageService {

    @Value("${upload.path}") // Lấy đường dẫn thư mục lưu ảnh từ application.properties
    private String uploadPath;

    public boolean deleteImage(String imageName) {
        File file = new File(uploadPath + File.separator + imageName);
        return file.exists() && file.delete();
    }
}
