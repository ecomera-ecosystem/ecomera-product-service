package com.ecomera.product.batch.repository;

import com.ecomera.product.batch.model.ProductImport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImportRepository extends JpaRepository<ProductImport, UUID> {

    @Query("SELECT p FROM ProductImport p WHERE p.initiatedBy = :initiatedBy ORDER BY p.createdAt DESC")
    Page<ProductImport> findByInitiatedBy(@Param("initiatedBy") String initiatedBy, Pageable pageable);

    Optional<ProductImport> findByIdAndInitiatedBy(UUID id, String initiatedBy);
}