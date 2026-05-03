package com.ecomera.product.product.controller;

import com.ecomera.product.product.dto.ProductCreateDto;
import com.ecomera.product.product.dto.ProductDto;
import com.ecomera.product.product.dto.ProductUpdateDto;
import com.ecomera.product.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a new product", description = "Creates a product and returns the created resource")
    @ApiResponse(responseCode = "201", description = "Product created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid product data")
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductCreateDto productDTO) {
        ProductDto product = productService.saveProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @GetMapping
    @Operation(summary = "Get paginated list of products", description = "Returns products with pagination and sorting")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    public ResponseEntity<Page<ProductDto>> getAll(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", schema = @Schema(allowableValues = {"asc", "desc"})) @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(productService.getAllProducts(page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<ProductDto> getById(
            @Parameter(description = "Product UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/count")
    @Operation(summary = "Get product count", description = "Returns the total number of products. If a categoryId is provided, returns the count for that category.")
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<Long> getCount(
            @Parameter(description = "Category ID to filter by") @RequestParam(required = false) UUID categoryId) {
        return ResponseEntity.ok(
                (categoryId == null)
                        ? productService.countProducts()
                        : productService.countProductsByCategory(categoryId)
        );
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product by ID. Only provided fields will be updated.")
    @ApiResponse(responseCode = "200", description = "Product updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid product data")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<ProductDto> update(
            @Parameter(description = "Product UUID") @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateDto dto) {
        return ResponseEntity.ok(productService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Deletes a product by ID.")
    @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Product UUID") @PathVariable UUID id) {
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Searches products by query string with pagination.")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid search query")
    public ResponseEntity<Page<ProductDto>> search(
            @Parameter(description = "Search query string", example = "Laptop") @RequestParam String query,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.searchProducts(query, pageable));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Returns products filtered by category with pagination.")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<Page<ProductDto>> getByCategory(
            @Parameter(description = "Category UUID") @PathVariable UUID categoryId,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    @GetMapping("/title")
    @Operation(summary = "Get product by title", description = "Fetches a product by its title.")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<ProductDto> getByTitle(
            @Parameter(description = "Product title", example = "MacBook Pro M3") @RequestParam String title) {
        return ResponseEntity.ok(productService.getProductByTitle(title));
    }

    @GetMapping("/price")
    @Operation(summary = "Get products by price range", description = "Returns products within the specified price range with pagination.")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid price range")
    public ResponseEntity<Iterable<ProductDto>> getByPriceInRange(
            @Parameter(description = "Minimum price", example = "100.00") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum price", example = "2000.00") @RequestParam BigDecimal maxPrice,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByPriceBetweenRange(minPrice, maxPrice, pageable));
    }
}