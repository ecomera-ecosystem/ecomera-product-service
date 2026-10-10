package com.ecomera.product.batch.model;

public record ProductImportItem(
    String sku,
    String title,
    String description,
    String price,
    String stock,
    String categoryId,
    String color,
    String size,
    String imageUrl
) {
}