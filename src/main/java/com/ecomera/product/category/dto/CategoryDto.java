package com.ecomera.product.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record CategoryDto(
        @Schema(description = "Unique category ID") UUID id,
        @Schema(description = "Category name") String name,
        @Schema(description = "Category description") String description,
        @Schema(description = "Category image URL") String imageUrl,
        @Schema(description = "URL-friendly slug") String slug,
        @Schema(description = "Parent category ID") UUID parentId,
        @Schema(description = "Child categories") List<CategoryDto> children,
        @Schema(description = "Is category active") Boolean isActive,
        @Schema(description = "Display order") Integer displayOrder,
        @Schema(description = "Creation timestamp") LocalDateTime createdAt,
        @Schema(description = "Last update timestamp") LocalDateTime updatedAt
) {
}
