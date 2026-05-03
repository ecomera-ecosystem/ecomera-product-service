package com.ecomera.product.product.repository;

import com.ecomera.product.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(UUID productId);

    ProductImage findByProductIdAndIsPrimaryTrue(UUID productId);

    @Modifying
    void deleteByProductId(UUID productId);
}
