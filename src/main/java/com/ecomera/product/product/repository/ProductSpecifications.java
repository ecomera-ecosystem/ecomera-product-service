package com.ecomera.product.product.repository;

import com.ecomera.product.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> withFilters(
            String keyword,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String color,
            String size) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descriptionLike = cb.like(cb.lower(root.get("description")), pattern);
                Predicate categoryLike = cb.like(cb.lower(root.get("category").get("name")), pattern);
                predicates.add(cb.or(titleLike, descriptionLike, categoryLike));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (color != null && !color.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("color")), color.trim().toLowerCase()));
            }

            if (size != null && !size.trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("size")), size.trim().toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
