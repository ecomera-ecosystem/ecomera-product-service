package com.ecomera.product.category.service;

import com.ecomera.product.category.dto.CategoryCreateDto;
import com.ecomera.product.category.dto.CategoryDto;
import com.ecomera.product.category.dto.CategoryUpdateDto;
import com.ecomera.product.category.entity.Category;
import com.ecomera.product.category.mapper.CategoryMapper;
import com.ecomera.product.category.repository.CategoryRepository;
import com.ecomera.product.shared.common.exception.AlreadyExistException;
import com.ecomera.product.shared.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    @Caching(
            put = @CachePut(value = "categories", key = "#result.id"),
            evict = {
                    @CacheEvict(value = "categories-tree", allEntries = true),
                    @CacheEvict(value = "categories-active", allEntries = true)
            }
    )
    public CategoryDto saveCategory(CategoryCreateDto dto) {
        if (categoryRepository.existsBySlug(dto.slug())) {
            throw new AlreadyExistException(Category.class, "slug", dto.slug());
        }

        Category category = categoryMapper.toEntity(dto);
        category.setIsActive(true);

        if (dto.parentId() != null) {
            Category parent = categoryRepository.findById(dto.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException(Category.class, "id", dto.parentId()));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created: {} - {}", savedCategory.getId(), savedCategory.getName());
        return categoryMapper.toDto(savedCategory);
    }

    @Transactional
    @Caching(
            put = @CachePut(value = "categories", key = "#id"),
            evict = {
                    @CacheEvict(value = "categories-tree", allEntries = true),
                    @CacheEvict(value = "categories-active", allEntries = true)
            }
    )
    public CategoryDto update(UUID id, CategoryUpdateDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Category.class, "id", id));

        if (dto.slug() != null && !category.getSlug().equals(dto.slug()) && categoryRepository.existsBySlug(dto.slug())) {
            throw new AlreadyExistException(Category.class, "slug", dto.slug());
        }

        categoryMapper.updateEntityFromDto(dto, category);

        if (dto.parentId() != null) {
            Category parent = categoryRepository.findById(dto.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException(Category.class, "id", dto.parentId()));
            category.setParent(parent);
        }

        Category updated = categoryRepository.save(category);
        log.info("Category updated: {}", id);
        return categoryMapper.toDto(updated);
    }

    @Cacheable(value = "categories", key = "#id")
    public CategoryDto getCategoryById(UUID id) {
        log.debug("Cache miss fetching category {} from DB", id);
        return categoryRepository.findById(id)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(Category.class, "id", id));
    }

    @Cacheable(value = "categories-slug", key = "#slug")
    public CategoryDto getCategoryBySlug(String slug) {
        log.debug("Cache miss fetching category by slug: {}", slug);
        return categoryRepository.findBySlug(slug)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(Category.class, "slug", slug));
    }

    @Cacheable(value = "categories-tree", key = "'root'")
    public List<CategoryDto> getRootCategories() {
        log.debug("Cache miss fetching root categories from DB");
        return categoryRepository.findByParentIsNullOrderByDisplayOrderAsc()
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Cacheable(value = "categories-active", key = "'all'")
    public List<CategoryDto> getAllActiveCategories() {
        log.debug("Cache miss fetching active categories from DB");
        return categoryRepository.findAllActive()
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "categories", key = "#id"),
            @CacheEvict(value = "categories-tree", allEntries = true),
            @CacheEvict(value = "categories-active", allEntries = true),
            @CacheEvict(value = "categories-slug", allEntries = true)
    })
    public void deleteCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Category.class, "id", id));
        categoryRepository.delete(category);
        log.info("Category deleted: {}", id);
    }

    public long countCategories() {
        return categoryRepository.count();
    }
}
