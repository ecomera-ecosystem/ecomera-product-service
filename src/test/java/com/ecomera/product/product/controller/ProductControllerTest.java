package com.ecomera.product.product.controller;

import com.ecomera.product.product.dto.ProductCreateDto;
import com.ecomera.product.product.dto.ProductDto;
import com.ecomera.product.product.dto.ProductFilterCriteria;
import com.ecomera.product.product.dto.ProductUpdateDto;
import com.ecomera.product.product.service.ProductService;
import com.ecomera.product.shared.common.exception.BusinessException;
import com.ecomera.product.shared.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ProductService productService;

    private UUID productId;
    private UUID categoryId;
    private ProductCreateDto validCreateDto;
    private ProductDto sampleDto;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        validCreateDto = ProductCreateDto.builder()
                .title("MacBook Pro")
                .description("Powerful laptop")
                .price(new BigDecimal("1999.99"))
                .stock(50)
                .categoryId(categoryId)
                .build();

        sampleDto = ProductDto.builder()
                .id(productId)
                .title("MacBook Pro")
                .description("Powerful laptop")
                .price(new BigDecimal("1999.99"))
                .stock(50)
                .categoryId(categoryId)
                .categoryName("Electronics")
                .build();
    }

    @Test
    void shouldCreateProduct() throws Exception {
        given(productService.saveProduct(any(ProductCreateDto.class))).willReturn(sampleDto);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.title").value("MacBook Pro"))
                .andExpect(jsonPath("$.price").value(1999.99));

        verify(productService, times(1)).saveProduct(any(ProductCreateDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreateWithInvalidData() throws Exception {
        ProductCreateDto invalidDto = ProductCreateDto.builder()
                .title("")
                .price(null)
                .stock(null)
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).saveProduct(any());
    }

    @Test
    void shouldReturnBadRequestWhenCreateWithNullFields() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        Page<ProductDto> page = new PageImpl<>(List.of(sampleDto), PageRequest.of(0, 10), 1);
        given(productService.getAllProducts(anyInt(), anyInt(), anyString(), anyString())).willReturn(page);

        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(productId.toString()));
    }

    @Test
    void shouldGetProductById() throws Exception {
        given(productService.getProductById(productId)).willReturn(sampleDto);

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.title").value("MacBook Pro"));

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    void shouldReturnNotFoundWhenProductByIdNotFound() throws Exception {
        given(productService.getProductById(productId)).willThrow(new ResourceNotFoundException("Product", "id", productId));

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldGetProductCount() throws Exception {
        given(productService.countProducts()).willReturn(10L);

        mockMvc.perform(get("/api/v1/products/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }

    @Test
    void shouldGetProductCountByCategory() throws Exception {
        given(productService.countProductsByCategory(categoryId)).willReturn(5L);

        mockMvc.perform(get("/api/v1/products/count")
                        .param("categoryId", categoryId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        ProductUpdateDto updateDto = ProductUpdateDto.builder()
                .title("Updated MacBook")
                .description("Updated description")
                .build();

        ProductDto updatedDto = ProductDto.builder()
                .id(productId)
                .title("Updated MacBook")
                .description("Updated description")
                .price(new BigDecimal("1999.99"))
                .stock(50)
                .categoryId(categoryId)
                .build();

        given(productService.update(eq(productId), any(ProductUpdateDto.class))).willReturn(updatedDto);

        mockMvc.perform(patch("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated MacBook"));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        doNothing().when(productService).deleteProductById(productId);

        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProductById(productId);
    }

    @Test
    void shouldReturnNotFoundWhenDeleteProductNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Product", "id", productId))
                .when(productService).deleteProductById(productId);

        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSearchProducts() throws Exception {
        Page<ProductDto> page = new PageImpl<>(List.of(sampleDto), PageRequest.of(0, 10), 1);
        given(productService.searchProducts(eq("MacBook"), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/v1/products/search")
                        .param("query", "MacBook")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("MacBook Pro"));
    }

    @Test
    void shouldGetProductsByCategory() throws Exception {
        Page<ProductDto> page = new PageImpl<>(List.of(sampleDto), PageRequest.of(0, 10), 1);
        given(productService.getProductsByCategory(eq(categoryId), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/v1/products/category/{categoryId}", categoryId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].categoryId").value(categoryId.toString()));
    }

    @Test
    void shouldGetProductByTitle() throws Exception {
        given(productService.getProductByTitle("MacBook Pro")).willReturn(sampleDto);

        mockMvc.perform(get("/api/v1/products/title")
                        .param("title", "MacBook Pro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("MacBook Pro"));
    }

    @Test
    void shouldGetProductsByPriceRange() throws Exception {
        given(productService.getProductsByPriceBetweenRange(
                eq(new BigDecimal("100")), eq(new BigDecimal("2000")), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleDto)));

        mockMvc.perform(get("/api/v1/products/price")
                        .param("minPrice", "100")
                        .param("maxPrice", "2000")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldReturnBadRequestWhenMinPriceGreaterThanMaxPrice() throws Exception {
        given(productService.getProductsByPriceBetweenRange(
                any(BigDecimal.class), any(BigDecimal.class), any(Pageable.class)))
                .willThrow(new BusinessException("Min price cannot be greater than max price"));

        mockMvc.perform(get("/api/v1/products/price")
                        .param("minPrice", "2000")
                        .param("maxPrice", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFilterProducts() throws Exception {
        given(productService.filterProducts(any(ProductFilterCriteria.class), anyString(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleDto), PageRequest.of(0, 12), 1));

        mockMvc.perform(get("/api/v1/products/filter")
                        .param("q", "laptop")
                        .param("minPrice", "100.00")
                        .param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(productId.toString()));

        ArgumentCaptor<ProductFilterCriteria> criteriaCaptor = ArgumentCaptor.forClass(ProductFilterCriteria.class);
        verify(productService).filterProducts(criteriaCaptor.capture(), eq("price_asc"), any(Pageable.class));
        assertThat(criteriaCaptor.getValue().keyword()).isEqualTo("laptop");
        assertThat(criteriaCaptor.getValue().minPrice()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldReturnBadRequestWhenFilterSortInvalid() throws Exception {
        given(productService.filterProducts(any(ProductFilterCriteria.class), anyString(), any(Pageable.class)))
                .willThrow(new BusinessException("Invalid sort option: bogus"));

        mockMvc.perform(get("/api/v1/products/filter")
                        .param("sort", "bogus"))
                .andExpect(status().isBadRequest());
    }
}
