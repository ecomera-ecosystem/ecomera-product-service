package com.ecomera.product.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProductImageUpdateDto(
        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        @Schema(description = "Updated image URL", example = "https://example.com/images/product-updated.jpg")
        String imageUrl,

        @Size(max = 255, message = "Alt text must not exceed 255 characters")
        @Schema(description = "Updated alt text", example = "Updated description")
        String altText,

        @Schema(description = "Updated primary image flag", example = "true")
        Boolean isPrimary,

        @Schema(description = "Updated display order", example = "2")
        Integer displayOrder
) {
}
