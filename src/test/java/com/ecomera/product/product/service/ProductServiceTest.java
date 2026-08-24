package com.ecomera.product.product.service;

import com.ecomera.product.category.entity.Category;
import com.ecomera.product.category.repository.CategoryRepository;
import com.ecomera.product.shared.common.exception.BusinessException;
import com.ecomera.product.shared.common.exception.ResourceNotFoundException;
import com.ecomera.product.product.dto.ProductCreateDto;
import com.ecomera.product.product.dto.ProductDto;
import com.ecomera.product.product.dto.ProductFilterCriteria;
import com.ecomera.product.product.dto.ProductUpdateDto;
import com.ecomera.product.product.entity.Product;
import com.ecomera.product.product.mapper.ProductMapper;
import com.ecomera.product.product.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    ProductMapper productMapper;

    @InjectMocks
    ProductService productService;

    private Product product;
    private Category category;
    private List<Product> products;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .name("Electronics")
                .slug("electronics")
                .build();

        product = Product.builder()
                .id(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .title("Dell XPS 13")
                .description("Powerful and compact laptop")
                .price(java.math.BigDecimal.valueOf(1199.99))
                .stock(50)
                .category(category)
                .build();

        Product product2 = Product.builder()
                .id(UUID.fromString("32333333-3333-3333-3333-333333333332"))
                .title("Lenovo ThinkPad X1 Carbon")
                .description("Premium business laptop")
                .price(java.math.BigDecimal.valueOf(1199.99))
                .stock(50)
                .category(category)
                .build();

        products = List.of(product, product2);
    }

    @Test
    void shouldSaveProductSuccessfully() {
        ProductCreateDto createDto = ProductCreateDto.builder()
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(category.getId())
                .build();

        ProductDto expectedDto = ProductDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .build();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(productMapper.toEntity(createDto)).willReturn(product);
        given(productRepository.save(product)).willReturn(product);
        given(productMapper.toDto(product)).willReturn(expectedDto);

        ProductDto saved = productService.saveProduct(createDto);

        assertThat(saved).isNotNull();
        assertThat(saved.title()).isEqualTo(product.getTitle());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void shouldGetProductByIdSuccessfully() {
        ProductDto expectedDto = ProductDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .build();

        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
        given(productMapper.toDto(product)).willReturn(expectedDto);

        ProductDto actual = productService.getProductById(product.getId());

        assertThat(actual).isNotNull();
        assertThat(actual.title()).isEqualTo(product.getTitle());
        verify(productRepository, times(1)).findById(product.getId());
    }

    @Test
    void shouldThrowWhenProductNotFoundById() {
        given(productRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(product.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("id");

        verify(productRepository, times(1)).findById(product.getId());
    }

    @Test
    void shouldGetProductByTitleSuccessfully() {
        ProductDto expectedDto = ProductDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .build();

        given(productRepository.findByTitle(product.getTitle())).willReturn(product);
        given(productMapper.toDto(product)).willReturn(expectedDto);

        ProductDto actual = productService.getProductByTitle(product.getTitle());

        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(product.getId());
        verify(productRepository, times(1)).findByTitle(product.getTitle());
    }

    @Test
    void shouldThrowWhenProductNotFoundByTitle() {
        given(productRepository.findByTitle(any(String.class))).willReturn(null);

        assertThatThrownBy(() -> productService.getProductByTitle(product.getTitle()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, times(1)).findByTitle(product.getTitle());
    }

    @Test
    void shouldSearchProductsSuccessfully() {
        String query = "laptop";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        ProductDto expectedDto = ProductDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .build();

        given(productRepository.searchProducts(query, pageable)).willReturn(productPage);
        given(productMapper.toDto(any(Product.class))).willReturn(expectedDto);

        Page<ProductDto> actual = productService.searchProducts(query, pageable);

        assertThat(actual).isNotNull();
        assertThat(actual.getContent()).hasSize(products.size());
        verify(productRepository, times(1)).searchProducts(query, pageable);
    }

    @Test
    void shouldThrowWhenSearchQueryIsEmpty() {
        assertThatThrownBy(() -> productService.searchProducts("", PageRequest.of(0, 10)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Search query cannot be empty");
    }

    @Test
    void shouldThrowWhenSearchQueryIsNull() {
        assertThatThrownBy(() -> productService.searchProducts(null, PageRequest.of(0, 10)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Search query cannot be empty");
    }

    @Test
    void shouldGetProductsByCategorySuccessfully() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        ProductDto expectedDto = ProductDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .build();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(productRepository.findByCategory(category, pageable)).willReturn(productPage);
        given(productMapper.toDto(any(Product.class))).willReturn(expectedDto);

        Page<ProductDto> actual = productService.getProductsByCategory(category.getId(), pageable);

        assertThat(actual).isNotNull();
        assertThat(actual.getContent()).hasSize(products.size());
        verify(productRepository, times(1)).findByCategory(category, pageable);
    }

    @Test
    void shouldGetAllProductsSuccessfully() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("price").ascending());
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        ProductDto expectedDto = ProductDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .build();

        given(productRepository.findAll(any(Pageable.class))).willReturn(productPage);
        given(productMapper.toDto(any(Product.class))).willReturn(expectedDto);

        Page<ProductDto> actual = productService.getAllProducts(0, 2, "price", "asc");

        assertThat(actual).isNotNull();
        assertThat(actual.getContent()).hasSize(products.size());
        verify(productRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void shouldUpdateProductSuccessfully() {
        String newTitle = "Updated";
        ProductUpdateDto updateDto = ProductUpdateDto.builder()
                .title(newTitle)
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(category.getId())
                .build();
        product.setTitle(newTitle);

        ProductDto expectedDto = ProductDto.builder()
                .id(product.getId())
                .title(newTitle)
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .build();

        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(productRepository.save(product)).willReturn(product);
        given(productMapper.toDto(product)).willReturn(expectedDto);

        ProductDto actual = productService.update(product.getId(), updateDto);

        assertThat(actual).isNotNull();
        assertThat(actual.title()).isEqualTo(newTitle);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void shouldThrowWhenProductNotFoundOnUpdate() {
        given(productRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(product.getId(), any(ProductUpdateDto.class)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldDeleteProductSuccessfully() {
        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));

        productService.deleteProductById(product.getId());

        verify(productRepository, times(1)).findById(product.getId());
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    void shouldThrowWhenProductNotFoundOnDelete() {
        given(productRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProductById(product.getId()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).delete(any(Product.class));
    }

    private ProductDto sampleDto() {
        return ProductDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .build();
    }

    @SuppressWarnings("unchecked")
    private Page<ProductDto> filterAndCapturePageable(ProductFilterCriteria criteria, String sort) {
        Pageable input = PageRequest.of(1, 12);
        Page<Product> productPage = new PageImpl<>(products, input, products.size());
        given(productRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(productPage);
        given(productMapper.toDto(any(Product.class))).willReturn(sampleDto());

        Page<ProductDto> actual = productService.filterProducts(criteria, sort, input);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(12);
        return actual;
    }

    @Test
    void shouldFilterProductsWithDefaultSortApplied() {
        ProductFilterCriteria criteria = new ProductFilterCriteria(null, null, null, null, null, null);

        Page<ProductDto> actual = filterAndCapturePageable(criteria, null);

        assertThat(actual.getContent()).hasSize(2);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void shouldMapPriceAscSort() {
        filterAndCapturePageable(new ProductFilterCriteria(null, null, null, null, null, null), "price_asc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("price"))
                .isEqualTo(Sort.Order.asc("price"));
    }

    @Test
    void shouldMapPriceDescSortCaseInsensitive() {
        filterAndCapturePageable(new ProductFilterCriteria(null, null, null, null, null, null), "PRICE_DESC");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("price"))
                .isEqualTo(Sort.Order.desc("price"));
    }

    @Test
    void shouldMapRatingDescSort() {
        filterAndCapturePageable(new ProductFilterCriteria(null, null, null, null, null, null), "rating_desc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("rating"))
                .isEqualTo(Sort.Order.desc("rating"));
    }

    @Test
    void shouldMapNewestSortExplicitly() {
        filterAndCapturePageable(new ProductFilterCriteria(null, null, null, null, null, null), "newest");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt"))
                .isEqualTo(Sort.Order.desc("createdAt"));
    }

    @Test
    void shouldThrowWhenSortOptionIsInvalid() {
        ProductFilterCriteria criteria = new ProductFilterCriteria(null, null, null, null, null, null);

        assertThatThrownBy(() -> productService.filterProducts(criteria, "cheapest", PageRequest.of(0, 10)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid sort option");
    }

    @Test
    void shouldThrowWhenMinPriceGreaterThanMaxPriceOnFilter() {
        ProductFilterCriteria criteria = new ProductFilterCriteria(
                null, null,
                java.math.BigDecimal.valueOf(500),
                java.math.BigDecimal.valueOf(100),
                null, null);

        assertThatThrownBy(() -> productService.filterProducts(criteria, null, PageRequest.of(0, 10)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Min price cannot be greater than max price");

        verify(productRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }
}
