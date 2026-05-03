package com.ecomera.product.category.controller;

import com.ecomera.product.category.dto.CategoryCreateDto;
import com.ecomera.product.category.dto.CategoryDto;
import com.ecomera.product.category.dto.CategoryUpdateDto;
import com.ecomera.product.category.service.CategoryService;
import com.ecomera.product.shared.common.exception.AlreadyExistException;
import com.ecomera.product.shared.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CategoryService categoryService;

    private UUID categoryId;
    private CategoryCreateDto validCreateDto;
    private CategoryDto sampleDto;
    private CategoryDto rootDto;
    private CategoryDto childDto;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        validCreateDto = CategoryCreateDto.builder()
                .name("Electronics")
                .slug("electronics")
                .description("Electronic devices")
                .imageUrl("https://example.com/electronics.jpg")
                .displayOrder(1)
                .build();

        sampleDto = CategoryDto.builder()
                .id(categoryId)
                .name("Electronics")
                .slug("electronics")
                .description("Electronic devices")
                .isActive(true)
                .displayOrder(1)
                .children(List.of())
                .build();

        childDto = CategoryDto.builder()
                .id(UUID.randomUUID())
                .name("Laptops")
                .slug("laptops")
                .isActive(true)
                .parentId(categoryId)
                .children(List.of())
                .build();

        rootDto = CategoryDto.builder()
                .id(categoryId)
                .name("Electronics")
                .slug("electronics")
                .isActive(true)
                .children(List.of(childDto))
                .build();
    }

    @Test
    void shouldCreateCategory() throws Exception {
        given(categoryService.saveCategory(any(CategoryCreateDto.class))).willReturn(sampleDto);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.slug").value("electronics"));

        verify(categoryService, times(1)).saveCategory(any(CategoryCreateDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreateWithInvalidData() throws Exception {
        CategoryCreateDto invalidDto = CategoryCreateDto.builder()
                .name("")
                .slug("")
                .build();

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).saveCategory(any());
    }

    @Test
    void shouldReturnConflictWhenSlugAlreadyExists() throws Exception {
        given(categoryService.saveCategory(any(CategoryCreateDto.class)))
                .willThrow(new AlreadyExistException("Category", "slug", "electronics"));

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldGetAllActiveCategories() throws Exception {
        given(categoryService.getAllActiveCategories()).willReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(categoryId.toString()))
                .andExpect(jsonPath("$[0].name").value("Electronics"));
    }

    @Test
    void shouldGetCategoryById() throws Exception {
        given(categoryService.getCategoryById(categoryId)).willReturn(sampleDto);

        mockMvc.perform(get("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Electronics"));

        verify(categoryService, times(1)).getCategoryById(categoryId);
    }

    @Test
    void shouldReturnNotFoundWhenCategoryByIdNotFound() throws Exception {
        given(categoryService.getCategoryById(categoryId))
                .willThrow(new ResourceNotFoundException("Category", "id", categoryId));

        mockMvc.perform(get("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldGetCategoryBySlug() throws Exception {
        given(categoryService.getCategoryBySlug("electronics")).willReturn(sampleDto);

        mockMvc.perform(get("/api/v1/categories/slug/{slug}", "electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("electronics"));

        verify(categoryService, times(1)).getCategoryBySlug("electronics");
    }

    @Test
    void shouldReturnNotFoundWhenCategoryBySlugNotFound() throws Exception {
        given(categoryService.getCategoryBySlug("nonexistent"))
                .willThrow(new ResourceNotFoundException("Category", "slug", "nonexistent"));

        mockMvc.perform(get("/api/v1/categories/slug/{slug}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetRootCategories() throws Exception {
        given(categoryService.getRootCategories()).willReturn(List.of(rootDto));

        mockMvc.perform(get("/api/v1/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].children").isArray())
                .andExpect(jsonPath("$[0].children[0].name").value("Laptops"));

        verify(categoryService, times(1)).getRootCategories();
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        CategoryUpdateDto updateDto = CategoryUpdateDto.builder()
                .name("Updated Electronics")
                .description("Updated description")
                .isActive(true)
                .build();

        CategoryDto updatedDto = CategoryDto.builder()
                .id(categoryId)
                .name("Updated Electronics")
                .slug("electronics")
                .description("Updated description")
                .isActive(true)
                .build();

        given(categoryService.update(eq(categoryId), any(CategoryUpdateDto.class))).willReturn(updatedDto);

        mockMvc.perform(patch("/api/v1/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Electronics"));
    }

    @Test
    void shouldReturnNotFoundWhenCategoryNotFoundOnUpdate() throws Exception {
        CategoryUpdateDto updateDto = CategoryUpdateDto.builder()
                .name("Updated")
                .build();

        given(categoryService.update(eq(categoryId), any(CategoryUpdateDto.class)))
                .willThrow(new ResourceNotFoundException("Category", "id", categoryId));

        mockMvc.perform(patch("/api/v1/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        doNothing().when(categoryService).deleteCategoryById(categoryId);

        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategoryById(categoryId);
    }

    @Test
    void shouldReturnNotFoundWhenDeleteCategoryNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Category", "id", categoryId))
                .when(categoryService).deleteCategoryById(categoryId);

        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetCategoryCount() throws Exception {
        given(categoryService.countCategories()).willReturn(10L);

        mockMvc.perform(get("/api/v1/categories/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }
}
