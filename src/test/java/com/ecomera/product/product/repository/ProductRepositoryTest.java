package com.ecomera.product.product.repository;

import com.ecomera.product.category.entity.Category;
import com.ecomera.product.category.repository.CategoryRepository;
import com.ecomera.product.product.entity.Product;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(ProductRepositoryTest.AuditingConfig.class)
class ProductRepositoryTest {

    @TestConfiguration
    @EnableJpaAuditing
    static class AuditingConfig {
        @Bean
        AuditorAware<String> auditorProvider() {
            return () -> Optional.of("test-system");
        }
    }

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    private Category electronicsCategory;
    private Category clothingCategory;

    @BeforeEach
    void setUp() {
        electronicsCategory = Category.builder()
                .name("Electronics")
                .slug("electronics")
                .isActive(true)
                .build();

        clothingCategory = Category.builder()
                .name("Clothing")
                .slug("clothing")
                .isActive(true)
                .build();

        categoryRepository.saveAll(List.of(electronicsCategory, clothingCategory));

        Product laptop = Product.builder()
                .title("MacBook Pro M3")
                .description("Apple laptop with M3 chip")
                .price(new BigDecimal("1999.99"))
                .stock(50)
                .category(electronicsCategory)
                .build();

        Product phone = Product.builder()
                .title("iPhone 15 Pro")
                .description("Apple smartphone")
                .price(new BigDecimal("999.99"))
                .stock(100)
                .category(electronicsCategory)
                .build();

        Product tshirt = Product.builder()
                .title("Cotton T-Shirt")
                .description("Basic cotton t-shirt")
                .price(new BigDecimal("29.99"))
                .stock(500)
                .category(clothingCategory)
                .build();

        productRepository.saveAll(List.of(laptop, phone, tshirt));
    }

    @Test
    void shouldSearchProductsByTitle() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> results = productRepository.searchProducts("MacBook", pageable);
        assertThat(results).hasSize(1);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("MacBook Pro M3");
    }

    @Test
    void shouldSearchProductsByDescription() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> results = productRepository.searchProducts("smartphone", pageable);
        assertThat(results).hasSize(1);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("iPhone 15 Pro");
    }

    @Test
    void shouldSearchProductsByCategoryName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> results = productRepository.searchProducts("Clothing", pageable);
        assertThat(results).hasSize(1);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("Cotton T-Shirt");
    }

    @Test
    void shouldReturnEmptyPageWhenSearchNoMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> results = productRepository.searchProducts("nonexistent", pageable);
        assertThat(results).isEmpty();
    }

    @Test
    void shouldFindByCategory() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> results = productRepository.findByCategory(electronicsCategory, pageable);
        assertThat(results).hasSize(2);
        assertThat(results.getContent()).allMatch(p -> p.getCategory().equals(electronicsCategory));
    }

    @Test
    void shouldFindByPriceBetween() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> results = productRepository.findByPriceBetween(
                new BigDecimal("50"), new BigDecimal("1000"), pageable);
        assertThat(results).hasSize(1);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("iPhone 15 Pro");
    }

    @Test
    void shouldCountProductsByCategory() {
        assertThat(productRepository.countProductsByCategory(electronicsCategory)).isEqualTo(2);
    }

    @Test
    void shouldFindAllWithPagination() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Product> results = productRepository.findAll(pageable);
        assertThat(results).hasSize(2);
        assertThat(results.getTotalElements()).isEqualTo(3);
    }
}
