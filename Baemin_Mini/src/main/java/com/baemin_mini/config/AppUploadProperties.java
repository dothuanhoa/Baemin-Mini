package com.baemin_mini.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.upload")
public record AppUploadProperties(String rootDir, String publicPath, long maxImageSizeMb) {
}
