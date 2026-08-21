package com.ecomera.product.product.service;

import com.ecomera.product.category.entity.Category;
import com.ecomera.product.category.repository.CategoryRepository;
import com.ecomera.product.product.repository.ProductSpecifications;
import com.ecomera.product.shared.common.exception.BusinessException;
import com.ecomera.product.shared.common.exception.ResourceNotFoundException;
import com.ecomera.product.product.dto.ProductCreateDto;
import com.ecomera.product.product.dto.ProductDto;
import com.ecomera.product.product.dto.ProductImageCreateDto;
import com.ecomera.product.product.dto.ProductImageUpdateDto;
import com.ecomera.product.product.dto.ProductUpdateDto;
import com.ecomera.product.product.entity.Product;
import com.ecomera.product.product.entity.ProductImage;
import com.ecomera.product.product.mapper.ProductMapper;
import com.ecomera.product.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Transactional
    @Caching(
            put = @CachePut(value = "products", key = "#result.id"),
            evict = {
                    @CacheEvict(value = "products-page", allEntries = true),
                    @CacheEvict(value = "products-search", allEntries = true),
                    @CacheEvict(value = "products-category", allEntries = true),
                    @CacheEvict(value = "products-title", allEntries = true)
            }
    )
    public ProductDto saveProduct(ProductCreateDto dto) {
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(Category.class, "id", dto.categoryId()));

        Product product = productMapper.toEntity(dto);
        product.setCategory(category);

        if (dto.images() != null && !dto.images().isEmpty()) {
            List<ProductImage> images = new ArrayList<>();
            for (int i = 0; i < dto.images().size(); i++) {
                ProductImageCreateDto imageDto = dto.images().get(i);
                ProductImage image = productMapper.toEntity(imageDto);
                image.setProduct(product);
                if (image.getDisplayOrder() == null) {
                    image.setDisplayOrder(i);
                }
                if (image.getIsPrimary() == null) {
                    image.setIsPrimary(i == 0);
                }
                images.add(image);
            }
            product.setImages(images);
        }

        Product savedProduct = productRepository.save(product);
        log.info("Product created: {} - {}", savedProduct.getId(), savedProduct.getTitle());
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    @Caching(
            put = @CachePut(value = "products", key = "#id"),
            evict = {
                    @CacheEvict(value = "products-page", allEntries = true),
                    @CacheEvict(value = "products-search", allEntries = true),
                    @CacheEvict(value = "products-category", allEntries = true),
                    @CacheEvict(value = "products-title", allEntries = true)
            }
    )
    public ProductDto update(UUID id, ProductUpdateDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Product.class, "id", id));
        productMapper.updateEntityFromDto(dto, product);

        if (dto.categoryId() != null) {
            Category category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(Category.class, "id", dto.categoryId()));
            product.setCategory(category);
        }

        if (dto.images() != null) {
            product.getImages().clear();
            for (int i = 0; i < dto.images().size(); i++) {
                ProductImageUpdateDto imageDto = dto.images().get(i);
                if (imageDto.imageUrl() != null) {
                    ProductImage image = product.getImages().stream()
                            .filter(img -> img.getImageUrl().equals(imageDto.imageUrl()))
                            .findFirst()
                            .orElseGet(() -> {
                                ProductImage newImage = new ProductImage();
                                newImage.setProduct(product);
                                product.getImages().add(newImage);
                                return newImage;
                            });
                    productMapper.updateEntityFromDto(imageDto, image);
                    if (image.getDisplayOrder() == null) {
                        image.setDisplayOrder(i);
                    }
                }
            }
        }

        Product updated = productRepository.save(product);
        log.info("Product updated: {}", id);
        return productMapper.toDto(updated);
    }

    @Cacheable(value = "products", key = "#id")
    public ProductDto getProductById(UUID id) {
        log.debug("Cache miss fetching product {} from DB", id);
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(Product.class, "id", id));
    }

    @Cacheable(value = "products-page", key = "#page + '-' + #size + '-' + #sortBy + '-' + #direction")
    public Page<ProductDto> getAllProducts(int page, int size, String sortBy, String direction) {
        log.debug("Cache miss fetching all products from DB");
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable).map(productMapper::toDto);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products-page", allEntries = true),
            @CacheEvict(value = "products-search", allEntries = true),
            @CacheEvict(value = "products-category", allEntries = true),
            @CacheEvict(value = "products-title", allEntries = true)
    })
    public void deleteProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Product.class, "id", id));
        productRepository.delete(product);
        log.info("Product deleted: {}", id);
    }

    public long countProducts() {
        return productRepository.count();
    }

    public long countProductsByCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(Category.class, "id", categoryId));
        return productRepository.countProductsByCategory(category);
    }

    @Cacheable(value = "products-search", key = "#query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductDto> searchProducts(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            throw new BusinessException("Search query cannot be empty");
        }
        log.debug("Cache miss - searching products for query: {}", query);
        return productRepository.searchProducts(query, pageable).map(productMapper::toDto);
    }

    @Cacheable(value = "products-category", key = "#categoryId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductDto> getProductsByCategory(UUID categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(Category.class, "id", categoryId));
        log.info("Cache miss - fetching products by category: {}", category.getName());
        return productRepository.findByCategory(category, pageable).map(productMapper::toDto);
    }

    @Cacheable(value = "products-title", key = "#title")
    public ProductDto getProductByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException("Title cannot be null or empty");
        }
        log.debug("Cache miss - fetching product by title: {}", title);
        Product product = productRepository.findByTitle(title);
        if (product == null) {
            throw new ResourceNotFoundException(Product.class, "title", title);
        }
        return productMapper.toDto(product);
    }

    public Page<ProductDto> getProductsByPriceBetweenRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        if (minPrice == null || maxPrice == null) {
            throw new BusinessException("Price range cannot be null");
        }
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException("Min price cannot be greater than max price");
        }
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable).map(productMapper::toDto);
    }

    public Page<ProductDto> filterProducts(
            String keyword,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String color,
            String size,
            String sort,
            Pageable pageable) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException("Min price cannot be greater than max price");
        }
        Sort mappedSort = mapSort(sort);
        Pageable effectivePageable = mappedSort == null
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), mappedSort);
        return productRepository
                .findAll(ProductSpecifications.withFilters(keyword, categoryId, minPrice, maxPrice, color, size), effectivePageable)
                .map(productMapper::toDto);
    }

    private Sort mapSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sort.toLowerCase()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "rating_desc" -> Sort.by(Sort.Direction.DESC, "rating");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> throw new BusinessException(
                    "Invalid sort option: " + sort + ". Allowed values: newest, price_asc, price_desc, rating_desc");
        };
    }
}
