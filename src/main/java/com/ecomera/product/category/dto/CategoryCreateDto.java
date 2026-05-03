package com.ecomera.product.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CategoryCreateDto(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        @Schema(description = "Category name") String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        @Schema(description = "Category description") String description,

        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        @Schema(description = "Category image URL") String imageUrl,

        @NotBlank(message = "Slug is required")
        @Size(max = 100, message = "Slug must not exceed 100 characters")
        @Schema(description = "URL-friendly slug") String slug,

        @Schema(description = "Parent category ID") UUID parentId,

        @Schema(description = "Display order") Integer displayOrder
) {
}
