package com.ecomera.product.category.repository;

import com.ecomera.product.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Category> findByParentIsNullOrderByDisplayOrderAsc();

    @Query("SELECT c FROM Category c WHERE c.parent = :parent ORDER BY c.displayOrder ASC")
    List<Category> findByParent(@Param("parent") Category parent);

    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findAllActive();
}
