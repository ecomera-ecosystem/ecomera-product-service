package com.ecomera.product.category.service;

import com.ecomera.product.category.dto.CategoryCreateDto;
import com.ecomera.product.category.dto.CategoryDto;
import com.ecomera.product.category.dto.CategoryUpdateDto;
import com.ecomera.product.category.entity.Category;
import com.ecomera.product.category.mapper.CategoryMapper;
import com.ecomera.product.category.repository.CategoryRepository;
import com.ecomera.product.shared.common.exception.AlreadyExistException;
import com.ecomera.product.shared.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    CategoryMapper categoryMapper;

    @InjectMocks
    CategoryService categoryService;

    private Category category;
    private Category parent;

    @BeforeEach
    void setUp() {
        parent = Category.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .name("Electronics")
                .slug("electronics")
                .isActive(true)
                .build();

        category = Category.builder()
                .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .name("Laptops")
                .slug("laptops")
                .description("Laptop computers")
                .isActive(true)
                .displayOrder(1)
                .parent(parent)
                .children(List.of())
                .build();
    }

    @Test
    void shouldSaveCategorySuccessfully() {
        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(parent.getId())
                .displayOrder(1)
                .build();

        CategoryDto expectedDto = CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(parent.getId())
                .isActive(true)
                .displayOrder(1)
                .children(List.of())
                .build();

        given(categoryRepository.existsBySlug(createDto.slug())).willReturn(false);
        given(categoryRepository.findById(parent.getId())).willReturn(Optional.of(parent));
        given(categoryMapper.toEntity(createDto)).willReturn(category);
        given(categoryRepository.save(category)).willReturn(category);
        given(categoryMapper.toDto(category)).willReturn(expectedDto);

        CategoryDto saved = categoryService.saveCategory(createDto);

        assertThat(saved).isNotNull();
        assertThat(saved.name()).isEqualTo(category.getName());
        assertThat(saved.isActive()).isTrue();
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void shouldThrowWhenSlugAlreadyExistsOnCreate() {
        CategoryCreateDto createDto = CategoryCreateDto.builder()
                .name("Duplicate")
                .slug("electronics")
                .build();

        given(categoryRepository.existsBySlug("electronics")).willReturn(true);

        assertThatThrownBy(() -> categoryService.saveCategory(createDto))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("slug");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldGetCategoryByIdSuccessfully() {
        CategoryDto expectedDto = CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .isActive(true)
                .children(List.of())
                .build();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(categoryMapper.toDto(category)).willReturn(expectedDto);

        CategoryDto actual = categoryService.getCategoryById(category.getId());

        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(category.getId());
        assertThat(actual.name()).isEqualTo(category.getName());
        verify(categoryRepository, times(1)).findById(category.getId());
    }

    @Test
    void shouldThrowWhenCategoryNotFoundById() {
        UUID id = UUID.randomUUID();
        given(categoryRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("id");
    }

    @Test
    void shouldGetCategoryBySlugSuccessfully() {
        CategoryDto expectedDto = CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .isActive(true)
                .build();

        given(categoryRepository.findBySlug(category.getSlug())).willReturn(Optional.of(category));
        given(categoryMapper.toDto(category)).willReturn(expectedDto);

        CategoryDto actual = categoryService.getCategoryBySlug(category.getSlug());

        assertThat(actual).isNotNull();
        assertThat(actual.slug()).isEqualTo(category.getSlug());
        verify(categoryRepository, times(1)).findBySlug(category.getSlug());
    }

    @Test
    void shouldThrowWhenCategoryNotFoundBySlug() {
        given(categoryRepository.findBySlug("nonexistent")).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryBySlug("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("slug");
    }

    @Test
    void shouldUpdateCategorySuccessfully() {
        CategoryUpdateDto updateDto = CategoryUpdateDto.builder()
                .name("Updated Laptops")
                .description("Updated description")
                .isActive(true)
                .build();
        category.setName("Updated Laptops");

        CategoryDto expectedDto = CategoryDto.builder()
                .id(category.getId())
                .name("Updated Laptops")
                .slug(category.getSlug())
                .isActive(true)
                .build();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(categoryRepository.save(category)).willReturn(category);
        given(categoryMapper.toDto(category)).willReturn(expectedDto);

        CategoryDto actual = categoryService.update(category.getId(), updateDto);

        assertThat(actual).isNotNull();
        assertThat(actual.name()).isEqualTo("Updated Laptops");
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void shouldThrowWhenSlugAlreadyExistsOnUpdate() {
        CategoryUpdateDto updateDto = CategoryUpdateDto.builder()
                .slug("taken-slug")
                .build();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(categoryRepository.existsBySlug("taken-slug")).willReturn(true);

        assertThatThrownBy(() -> categoryService.update(category.getId(), updateDto))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining("slug");
    }

    @Test
    void shouldNotCheckSlugWhenSameSlugOnUpdate() {
        CategoryUpdateDto updateDto = CategoryUpdateDto.builder()
                .name("New Name")
                .slug(category.getSlug())
                .build();
        category.setName("New Name");

        CategoryDto expectedDto = CategoryDto.builder()
                .id(category.getId())
                .name("New Name")
                .slug(category.getSlug())
                .build();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(categoryRepository.save(category)).willReturn(category);
        given(categoryMapper.toDto(category)).willReturn(expectedDto);

        CategoryDto actual = categoryService.update(category.getId(), updateDto);

        assertThat(actual).isNotNull();
        verify(categoryRepository, never()).existsBySlug(any());
    }

    @Test
    void shouldThrowWhenCategoryNotFoundOnUpdate() {
        given(categoryRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(UUID.randomUUID(), any(CategoryUpdateDto.class)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldDeleteCategorySuccessfully() {
        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));

        categoryService.deleteCategoryById(category.getId());

        verify(categoryRepository, times(1)).findById(category.getId());
        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void shouldThrowWhenCategoryNotFoundOnDelete() {
        given(categoryRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategoryById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void shouldGetRootCategoriesSuccessfully() {
        List<Category> rootCategories = List.of(parent);
        CategoryDto parentDto = CategoryDto.builder()
                .id(parent.getId())
                .name(parent.getName())
                .slug(parent.getSlug())
                .build();

        given(categoryRepository.findByParentIsNullOrderByDisplayOrderAsc()).willReturn(rootCategories);
        given(categoryMapper.toDto(parent)).willReturn(parentDto);

        List<CategoryDto> actual = categoryService.getRootCategories();

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).name()).isEqualTo(parent.getName());
        verify(categoryRepository, times(1)).findByParentIsNullOrderByDisplayOrderAsc();
    }

    @Test
    void shouldGetAllActiveCategoriesSuccessfully() {
        List<Category> activeCategories = List.of(category);
        CategoryDto categoryDto = CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .isActive(true)
                .build();

        given(categoryRepository.findAllActive()).willReturn(activeCategories);
        given(categoryMapper.toDto(category)).willReturn(categoryDto);

        List<CategoryDto> actual = categoryService.getAllActiveCategories();

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).isActive()).isTrue();
        verify(categoryRepository, times(1)).findAllActive();
    }

    @Test
    void shouldCountCategoriesSuccessfully() {
        given(categoryRepository.count()).willReturn(5L);

        long count = categoryService.countCategories();

        assertThat(count).isEqualTo(5L);
        verify(categoryRepository, times(1)).count();
    }
}
