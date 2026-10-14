package com.ecomera.product.batch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ProductImportLaunchResponse(
        @Schema(description = "Import ID") UUID importId,
        @Schema(description = "Status URL to check import progress") String statusUrl,
        @Schema(description = "Launch timestamp") LocalDateTime launchedAt
) {
}