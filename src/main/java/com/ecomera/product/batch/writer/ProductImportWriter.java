package com.ecomera.product.batch.writer;

import com.ecomera.product.batch.model.ProductImportItem;
import com.ecomera.product.category.repository.CategoryRepository;
import com.ecomera.product.product.entity.Product;
import com.ecomera.product.product.entity.ProductImage;
import com.ecomera.product.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductImportWriter implements ItemWriter<ProductImportItem> {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void write(Chunk<? extends ProductImportItem> chunk) {
        for (ProductImportItem item : chunk.getItems()) {
            Product product = productRepository.findBySku(item.getSku())
                    .orElseGet(() -> Product.builder().sku(item.getSku()).build());

            product.setTitle(item.getTitle());
            product.setDescription(item.getDescription());
            product.setPrice(new BigDecimal(item.getPrice()));
            product.setStock(item.getStock());

            UUID categoryId = item.getCategoryId();
            product.setCategory(categoryRepository.getReferenceById(categoryId));

            product.setColor(item.getColor().isBlank() ? null : item.getColor());
            product.setSize(item.getSize().isBlank() ? null : item.getSize());

            if (item.getImageUrl() != null && !item.getImageUrl().isBlank() && product.getImages().isEmpty()) {
                product.getImages().add(ProductImage.builder()
                        .imageUrl(item.getImageUrl())
                        .altText(item.getTitle())
                        .isPrimary(true)
                        .displayOrder(1)
                        .build());
            }

            productRepository.save(product);
        }
    }
}