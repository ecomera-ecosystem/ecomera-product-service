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
            Product product = productRepository.findBySku(item.sku())
                    .orElseGet(() -> Product.builder().sku(item.sku()).build());

            product.setTitle(item.title());
            product.setDescription(item.description());
            product.setPrice(new BigDecimal(item.price()));
            product.setStock(Integer.parseInt(item.stock()));

            UUID categoryId = UUID.fromString(item.categoryId());
            product.setCategory(categoryRepository.getReferenceById(categoryId));

            product.setColor(item.color().isBlank() ? null : item.color());
            product.setSize(item.size().isBlank() ? null : item.size());

            if (item.imageUrl() != null && !item.imageUrl().isBlank() && product.getImages().isEmpty()) {
                product.getImages().add(ProductImage.builder()
                        .imageUrl(item.imageUrl())
                        .altText(item.title())
                        .isPrimary(true)
                        .displayOrder(1)
                        .build());
            }

            productRepository.save(product);
        }
    }
}