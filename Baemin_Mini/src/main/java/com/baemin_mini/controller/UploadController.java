package com.baemin_mini.controller;

import com.baemin_mini.common.ApiResponse;
import com.baemin_mini.dto.upload.ImageUploadResponse;
import com.baemin_mini.service.ImageUploadService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadController {
    private final ImageUploadService imageUploadService;

    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageUploadResponse> uploadImage(Principal principal, @RequestParam("file") MultipartFile file) {
        return ApiResponse.success("Image uploaded", imageUploadService.uploadImage(principal.getName(), file));
    }
}
