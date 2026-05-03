package com.ecomera.product.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ProductImageDto(
        @Schema(description = "Unique product image ID") UUID id,
        @Schema(description = "Image URL") String imageUrl,
        @Schema(description = "Alt text for accessibility and SEO") String altText,
        @Schema(description = "Whether this is the primary product image") Boolean isPrimary,
        @Schema(description = "Display order in gallery") Integer displayOrder
) {
}
