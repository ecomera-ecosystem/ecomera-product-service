package com.ecomera.product.batch.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "product_import_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "import_id", nullable = false)
    private UUID importId;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "sku", length = 64)
    private String sku;

    @Column(name = "title")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", length = 50)
    private String price;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "color")
    private String color;

    @Column(name = "size")
    private String size;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32)
    private ImportItemStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}