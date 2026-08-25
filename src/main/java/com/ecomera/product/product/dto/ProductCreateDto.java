package com.ecomera.product.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Schema(description = "DTO for creating a new product")
public record ProductCreateDto(
        @NotBlank(message = "Product title is required and cannot be blank")
        @Schema(description = "Product title", example = "MacBook Pro M3")
        String title,

        @Schema(description = "Detailed description of the product",
                example = "Latest Apple MacBook Pro with M3 chip")
        String description,

        @Schema(description = "List of product images")
        List<ProductImageCreateDto> images,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than zero")
        @Schema(description = "Price of the product", example = "1999.99")
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        @Schema(description = "Available stock quantity", example = "50")
        Integer stock,

        @NotNull(message = "Category is required")
        @Schema(description = "Category ID of the product", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        UUID categoryId,

        @Size(max = 50, message = "Color must not exceed 50 characters")
        @Schema(description = "Product color", example = "Black")
        String color,

        @Size(max = 20, message = "Size must not exceed 20 characters")
        @Schema(description = "Product size", example = "M")
        String size,

        @PositiveOrZero(message = "Rating cannot be negative")
        @Digits(integer = 2, fraction = 1, message = "Rating must be a valid decimal value")
        @Schema(description = "Average rating (0-5)", example = "4.5")
        BigDecimal rating
) {
}
