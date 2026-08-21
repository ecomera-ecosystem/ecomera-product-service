package com.ecomera.product.product.entity;

import com.ecomera.product.category.entity.Category;
import com.ecomera.product.shared.common.audit.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(nullable = false)
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must be a valid monetary amount")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private Integer stock;

    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    @Column(name = "color", length = 50)
    private String color;

    @Size(max = 20, message = "Size must not exceed 20 characters")
    @Column(name = "size", length = 20)
    private String size;

    @DecimalMin(value = "0.0", message = "Rating cannot be negative")
    @Digits(integer = 2, fraction = 1, message = "Rating must be a valid decimal value")
    @Column(name = "rating", precision = 3, scale = 1, nullable = false)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();
}
