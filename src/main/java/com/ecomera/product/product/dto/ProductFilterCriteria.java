package com.ecomera.product.product.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductFilterCriteria(
        String keyword,
        UUID categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String color,
        String size) {
}
