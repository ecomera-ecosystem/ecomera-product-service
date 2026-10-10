package com.ecomera.product.batch.processor;

import com.ecomera.product.batch.model.ProductImportItem;
import com.ecomera.product.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class ProductImportProcessor implements ItemProcessor<ProductImportItem, ProductImportItem> {

    private final CategoryRepository categoryRepository;
    private final AtomicInteger rejected = new AtomicInteger();

    @Override
    public ProductImportItem process(ProductImportItem item) throws Exception {
        if (item.sku() == null || item.sku().isBlank()) {
            rejected.incrementAndGet();
            return null;
        }

        try {
            new BigDecimal(item.price());
        } catch (NumberFormatException e) {
            rejected.incrementAndGet();
            return null;
        }

        int stock;
        try {
            stock = Integer.parseInt(item.stock());
            if (stock < 0) {
                rejected.incrementAndGet();
                return null;
            }
        } catch (NumberFormatException e) {
            rejected.incrementAndGet();
            return null;
        }

        UUID categoryId;
        try {
            categoryId = UUID.fromString(item.categoryId());
        } catch (IllegalArgumentException e) {
            rejected.incrementAndGet();
            return null;
        }

        if (!categoryRepository.existsById(categoryId)) {
            rejected.incrementAndGet();
            return null;
        }

        return item;
    }

    public int getRejectedCount() {
        return rejected.get();
    }
}