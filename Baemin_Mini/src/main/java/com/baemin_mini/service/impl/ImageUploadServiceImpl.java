package com.baemin_mini.service.impl;

import com.baemin_mini.common.exception.BadRequestException;
import com.baemin_mini.common.exception.NotFoundException;
import com.baemin_mini.config.AppUploadProperties;
import com.baemin_mini.domain.entity.UploadedFile;
import com.baemin_mini.domain.entity.User;
import com.baemin_mini.dto.upload.ImageUploadResponse;
import com.baemin_mini.repository.UploadedFileRepository;
import com.baemin_mini.repository.UserRepository;
import com.baemin_mini.service.ImageUploadService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageUploadServiceImpl implements ImageUploadService {
    private static final long BYTES_PER_MB = 1024L * 1024L;
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.copyOf(CONTENT_TYPES.values());

    private final AppUploadProperties uploadProperties;
    private final UserRepository userRepository;
    private final UploadedFileRepository uploadedFileRepository;

    @Override
    @Transactional
    public ImageUploadResponse uploadImage(String username, MultipartFile file) {
        validate(file);
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String extension = extensionOf(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + "." + extension;
        Path uploadRoot = Path.of(uploadProperties.rootDir()).toAbsolutePath().normalize();
        Path target = uploadRoot.resolve(storedName).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new BadRequestException("Invalid upload path");
        }

        try {
            Files.createDirectories(uploadRoot);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BadRequestException("Could not store image");
        }

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setOwner(owner);
        uploadedFile.setFileName(safeOriginalFileName(file.getOriginalFilename()));
        uploadedFile.setStoredName(storedName);
        uploadedFile.setRelativePath(storedName);
        uploadedFile.setContentType(CONTENT_TYPES.get(extension));
        uploadedFile.setSizeBytes(file.getSize());
        UploadedFile saved = uploadedFileRepository.save(uploadedFile);

        return new ImageUploadResponse(saved.getId(), publicUrl(storedName), saved.getContentType(), saved.getSizeBytes());
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }
        long maxBytes = uploadProperties.maxImageSizeMb() * BYTES_PER_MB;
        if (file.getSize() > maxBytes) {
            throw new BadRequestException("Image must not exceed " + uploadProperties.maxImageSizeMb() + " MB");
        }
        String extension = extensionOf(file.getOriginalFilename());
        String contentType = file.getContentType();
        if (!CONTENT_TYPES.containsKey(extension) || contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Only jpg, jpeg, png and webp images are allowed");
        }
    }

    private String extensionOf(String originalFileName) {
        if (originalFileName == null) {
            return "";
        }
        String filename = Path.of(originalFileName).getFileName().toString();
        int lastDot = filename.lastIndexOf('.');
        return lastDot < 1 || lastDot == filename.length() - 1
                ? ""
                : filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    private String safeOriginalFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "image";
        }
        return Path.of(originalFileName).getFileName().toString();
    }

    private String publicUrl(String storedName) {
        String publicPath = uploadProperties.publicPath();
        String normalized = publicPath == null || publicPath.isBlank() ? "/uploads" : publicPath.trim();
        return (normalized.startsWith("/") ? normalized : "/" + normalized) + "/" + storedName;
    }
}
