package com.ecomera.product.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProductImageCreateDto(
        @NotBlank(message = "Image URL is required")
        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        @Pattern(regexp = "^(https?)://.*$", message = "Image URL must be a valid URL")
        @Schema(description = "Image URL", example = "https://example.com/images/product.jpg")
        String imageUrl,

        @Size(max = 255, message = "Alt text must not exceed 255 characters")
        @Schema(description = "Alt text for accessibility and SEO", example = "Front view of product")
        String altText,

        @Schema(description = "Whether this is the primary product image", example = "true")
        Boolean isPrimary,

        @Schema(description = "Display order in gallery", example = "1")
        Integer displayOrder
) {
}
