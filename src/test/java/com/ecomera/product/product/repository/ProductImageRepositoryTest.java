package com.ecomera.product.product.repository;

import com.ecomera.product.category.entity.Category;
import com.ecomera.product.category.repository.CategoryRepository;
import com.ecomera.product.product.entity.Product;
import com.ecomera.product.product.entity.ProductImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;



import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(ProductImageRepositoryTest.AuditingConfig.class)
class ProductImageRepositoryTest {

    @TestConfiguration
    @EnableJpaAuditing
    static class AuditingConfig {
        @Bean
        AuditorAware<String> auditorProvider() {
            return () -> Optional.of("test-system");
        }
    }

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .name("Electronics")
                .slug("electronics")
                .isActive(true)
                .build();
        categoryRepository.save(category);

        product = Product.builder()
                .title("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .category(category)
                .build();
        productRepository.save(product);

        ProductImage image1 = ProductImage.builder()
                .product(product)
                .imageUrl("https://example.com/image1.jpg")
                .isPrimary(true)
                .displayOrder(1)
                .build();

        ProductImage image2 = ProductImage.builder()
                .product(product)
                .imageUrl("https://example.com/image2.jpg")
                .isPrimary(false)
                .displayOrder(2)
                .build();

        productImageRepository.saveAll(List.of(image1, image2));
    }

    @Test
    void shouldFindByProductIdOrderByDisplayOrderAsc() {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId());

        assertThat(images).hasSize(2);
        assertThat(images.get(0).getDisplayOrder()).isEqualTo(1);
        assertThat(images.get(1).getDisplayOrder()).isEqualTo(2);
    }

    @Test
    void shouldFindByProductIdAndIsPrimaryTrue() {
        ProductImage primary = productImageRepository.findByProductIdAndIsPrimaryTrue(product.getId());

        assertThat(primary).isNotNull();
        assertThat(primary.getIsPrimary()).isTrue();
        assertThat(primary.getImageUrl()).isEqualTo("https://example.com/image1.jpg");
    }

    @Test
    void shouldDeleteByProductId() {
        productImageRepository.deleteByProductId(product.getId());

        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId());
        assertThat(images).isEmpty();
    }
}
