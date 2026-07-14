package com.baemin_mini.domain.entity;

import com.baemin_mini.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "uploaded_files")
public class UploadedFile extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "stored_name", nullable = false, unique = true, length = 255)
    private String storedName;

    @Column(name = "relative_path", nullable = false, length = 500)
    private String relativePath;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;
}
