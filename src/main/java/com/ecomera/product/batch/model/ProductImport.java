package com.ecomera.product.batch.model;

import com.ecomera.product.shared.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_import")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    @Column(name = "file_checksum", length = 64)
    private String fileChecksum;

    @Column(name = "initiated_by", nullable = false, length = 255)
    private String initiatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ImportStatus status;

    @Column(name = "total_rows")
    private Integer totalRows;

    @Column(name = "created_count")
    private Integer createdCount;

    @Column(name = "updated_count")
    private Integer updatedCount;

    @Column(name = "rejected_count")
    private Integer rejectedCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}