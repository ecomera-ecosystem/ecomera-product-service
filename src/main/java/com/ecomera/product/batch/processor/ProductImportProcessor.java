package com.ecomera.product.batch.processor;

import com.ecomera.product.batch.model.ImportItemStatus;
import com.ecomera.product.batch.model.ProductImportItem;
import com.ecomera.product.batch.repository.ProductImportItemRepository;
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
    private final ProductImportItemRepository itemRepository;
    private final AtomicInteger rejected = new AtomicInteger();

    @Override
    public ProductImportItem process(ProductImportItem item) throws Exception {
        // Save pending item
        ProductImportItem pendingItem = new ProductImportItem();
        pendingItem.setImportId(item.getImportId());
        pendingItem.setLineNumber(item.getLineNumber());
        pendingItem.setSku(item.getSku());
        pendingItem.setTitle(item.getTitle());
        pendingItem.setDescription(item.getDescription());
        pendingItem.setPrice(item.getPrice());
        pendingItem.setStock(item.getStock());
        pendingItem.setCategoryId(item.getCategoryId());
        pendingItem.setColor(item.getColor());
        pendingItem.setSize(item.getSize());
        pendingItem.setImageUrl(item.getImageUrl());
        pendingItem.setStatus(ImportItemStatus.PENDING);
        itemRepository.save(pendingItem);

        // Validate
        if (item.getSku() == null || item.getSku().isBlank()) {
            updateAsRejected(pendingItem, "SKU is required");
            return null;
        }

        try {
            new BigDecimal(item.getPrice());
        } catch (NumberFormatException e) {
            updateAsRejected(pendingItem, "Invalid price format: " + item.getPrice());
            return null;
        }

        int stock;
        try {
            int parsedStock = item.getStock();
            if (parsedStock < 0) {
                updateAsRejected(pendingItem, "Stock cannot be negative: " + item.getStock());
                return null;
            }
            stock = parsedStock;
        } catch (NumberFormatException e) {
            updateAsRejected(pendingItem, "Invalid stock format: " + item.getStock());
            return null;
        }

        UUID categoryId;
        try {
            categoryId = item.getCategoryId();
        } catch (IllegalArgumentException e) {
            updateAsRejected(pendingItem, "Invalid category ID format: " + item.getCategoryId());
            return null;
        }

        if (!categoryRepository.existsById(categoryId)) {
            updateAsRejected(pendingItem, "Category not found: " + item.getCategoryId());
            return null;
        }

        pendingItem.setStatus(ImportItemStatus.PROCESSED);
        itemRepository.save(pendingItem);
        return item;
    }

    private void updateAsRejected(ProductImportItem item, String errorMessage) {
        item.setStatus(ImportItemStatus.REJECTED);
        item.setErrorMessage(errorMessage);
        itemRepository.save(item);
    }

    public int getRejectedCount(UUID importId) {
        return Math.toIntExact(itemRepository.countByImportIdAndStatus(importId, ImportItemStatus.REJECTED));
    }
}