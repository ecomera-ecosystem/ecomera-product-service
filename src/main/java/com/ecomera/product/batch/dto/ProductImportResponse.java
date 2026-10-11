package com.ecomera.product.batch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ProductImportResponse(
        @Schema(description = "Import ID") UUID importId,
        @Schema(description = "Original filename") String filename,
        @Schema(description = "Import status") String status,
        @Schema(description = "Total rows processed") Integer totalRows,
        @Schema(description = "Products created") Integer createdCount,
        @Schema(description = "Products updated") Integer updatedCount,
        @Schema(description = "Rows rejected") Integer rejectedCount,
        @Schema(description = "Error message if failed") String errorMessage,
        @Schema(description = "Import started at") LocalDateTime startedAt,
        @Schema(description = "Import completed at") LocalDateTime completedAt
) {
}