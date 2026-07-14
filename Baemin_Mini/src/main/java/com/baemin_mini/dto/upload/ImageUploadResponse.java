package com.baemin_mini.dto.upload;

public record ImageUploadResponse(Long id, String imageUrl, String contentType, long sizeBytes) {
}
