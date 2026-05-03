package com.ecomera.product.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record ProductDto(
        @Schema(description = "Unique product ID") UUID id,
        @Schema(description = "Product title") String title,
        @Schema(description = "Detailed description") String description,
        @Schema(description = "Price in USD") BigDecimal price,
        @Schema(description = "Available stock") Integer stock,
        @Schema(description = "Product images") List<ProductImageDto> images,
        @Schema(description = "Category ID") UUID categoryId,
        @Schema(description = "Category name") String categoryName,
        @Schema(description = "Creation timestamp") LocalDateTime createdAt,
        @Schema(description = "Last update timestamp") LocalDateTime updatedAt
) {
}
