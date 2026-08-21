package com.ecomera.product.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Schema(description = "DTO for updating an existing product. All fields are optional.")
public record ProductUpdateDto(
        @Schema(description = "Updated product title", example = "MacBook Pro M3 - 16 inch")
        String title,

        @Schema(description = "Updated description", example = "Updated specs and details")
        String description,

        @Schema(description = "Updated list of product images")
        List<ProductImageUpdateDto> images,

        @PositiveOrZero
        @Schema(description = "Updated price", example = "2099.99")
        BigDecimal price,

        @Min(0)
        @Schema(description = "Updated stock quantity", example = "30")
        Integer stock,

        @Schema(description = "Updated Category ID of the product", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        UUID categoryId,

        @Size(max = 50, message = "Color must not exceed 50 characters")
        @Schema(description = "Updated product color", example = "Black")
        String color,

        @Size(max = 20, message = "Size must not exceed 20 characters")
        @Schema(description = "Updated product size", example = "M")
        String size,

        @PositiveOrZero(message = "Rating cannot be negative")
        @Digits(integer = 2, fraction = 1, message = "Rating must be a valid decimal value")
        @Schema(description = "Updated average rating (0-5)", example = "4.5")
        BigDecimal rating
) {
}
