package com.ecomera.product.category.controller;

import com.ecomera.product.category.dto.CategoryCreateDto;
import com.ecomera.product.category.dto.CategoryDto;
import com.ecomera.product.category.dto.CategoryUpdateDto;
import com.ecomera.product.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Category management APIs")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new category", description = "Creates a category and returns the created resource")
    @ApiResponse(responseCode = "201", description = "Category created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid category data")
    @ApiResponse(responseCode = "409", description = "Category with this slug already exists")
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody CategoryCreateDto dto) {
        CategoryDto category = categoryService.saveCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @GetMapping
    @Operation(summary = "Get all active categories", description = "Returns all active categories")
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    public ResponseEntity<List<CategoryDto>> getAllActive() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }

    @GetMapping("/tree")
    @Operation(summary = "Get root categories", description = "Returns root categories (without parent) with their children")
    @ApiResponse(responseCode = "200", description = "Root categories retrieved successfully")
    public ResponseEntity<List<CategoryDto>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    @ApiResponse(responseCode = "200", description = "Category found")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<CategoryDto> getById(
            @Parameter(description = "Category UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug")
    @ApiResponse(responseCode = "200", description = "Category found")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<CategoryDto> getBySlug(
            @Parameter(description = "Category slug") @PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update category", description = "Updates an existing category by ID. Only provided fields will be updated.")
    @ApiResponse(responseCode = "200", description = "Category updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid category data")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "409", description = "Category with this slug already exists")
    public ResponseEntity<CategoryDto> update(
            @Parameter(description = "Category UUID") @PathVariable UUID id,
            @Valid @RequestBody CategoryUpdateDto dto) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Deletes a category by ID.")
    @ApiResponse(responseCode = "204", description = "Category deleted successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Category UUID") @PathVariable UUID id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @Operation(summary = "Get category count", description = "Returns the total number of categories")
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    public ResponseEntity<Long> getCount() {
        return ResponseEntity.ok(categoryService.countCategories());
    }
}
