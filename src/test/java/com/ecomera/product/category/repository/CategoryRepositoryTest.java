package com.ecomera.product.category.repository;

import com.ecomera.product.category.entity.Category;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;



import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(CategoryRepositoryTest.AuditingConfig.class)
class CategoryRepositoryTest {

    @TestConfiguration
    @EnableJpaAuditing
    static class AuditingConfig {
        @Bean
        AuditorAware<String> auditorProvider() {
            return () -> Optional.of("test-system");
        }
    }

    @Autowired
    CategoryRepository categoryRepository;

    private Category rootCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        rootCategory = Category.builder()
                .name("Electronics")
                .slug("electronics")
                .description("Electronic devices")
                .isActive(true)
                .displayOrder(1)
                .build();

        childCategory = Category.builder()
                .name("Laptops")
                .slug("laptops")
                .description("Laptop computers")
                .isActive(true)
                .displayOrder(1)
                .parent(rootCategory)
                .build();

        categoryRepository.save(rootCategory);
        childCategory.setParent(rootCategory);
        categoryRepository.save(childCategory);
    }

    @Test
    void shouldFindBySlug() {
        Optional<Category> found = categoryRepository.findBySlug("electronics");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Electronics");
    }

    @Test
    void shouldReturnEmptyWhenSlugNotFound() {
        assertThat(categoryRepository.findBySlug("nonexistent")).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenSlugExists() {
        assertThat(categoryRepository.existsBySlug("electronics")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSlugNotExists() {
        assertThat(categoryRepository.existsBySlug("nonexistent")).isFalse();
    }

    @Test
    void shouldFindRootCategoriesOrderByDisplayOrder() {
        List<Category> roots = categoryRepository.findByParentIsNullOrderByDisplayOrderAsc();
        assertThat(roots).hasSize(1);
        assertThat(roots.get(0).getSlug()).isEqualTo("electronics");
    }

    @Test
    void shouldFindByParent() {
        List<Category> children = categoryRepository.findByParent(rootCategory);
        assertThat(children).hasSize(1);
        assertThat(children.get(0).getSlug()).isEqualTo("laptops");
    }

    @Test
    void shouldFindAllActiveCategories() {
        List<Category> active = categoryRepository.findAllActive();
        assertThat(active)
                .hasSize(2)
                .allMatch(Category::getIsActive);
    }

    @Test
    void shouldNotReturnInactiveCategories() {
        rootCategory.setIsActive(false);
        categoryRepository.save(rootCategory);
        List<Category> active = categoryRepository.findAllActive();
        assertThat(active).hasSize(1);
        assertThat(active.get(0).getSlug()).isEqualTo("laptops");
    }
}
