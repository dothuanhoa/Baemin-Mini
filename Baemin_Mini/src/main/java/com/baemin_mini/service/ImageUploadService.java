package com.baemin_mini.service;

import com.baemin_mini.dto.upload.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {
    ImageUploadResponse uploadImage(String username, MultipartFile file);
}
