package com.baemin_mini.config;

import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebResourceConfig implements WebMvcConfigurer {
    private final AppUploadProperties uploadProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String publicPath = normalizePublicPath(uploadProperties.publicPath());
        Path uploadRoot = Path.of(uploadProperties.rootDir()).toAbsolutePath().normalize();
        registry.addResourceHandler(publicPath + "/**")
                .addResourceLocations(uploadRoot.toUri().toString());
    }

    private String normalizePublicPath(String publicPath) {
        String normalized = publicPath == null || publicPath.isBlank() ? "/uploads" : publicPath.trim();
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }
}
