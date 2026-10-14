package com.ecomera.product.batch.repository;

import com.ecomera.product.batch.model.ImportItemStatus;
import com.ecomera.product.batch.model.ProductImportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductImportItemRepository extends JpaRepository<ProductImportItem, UUID> {

    @Query("SELECT p FROM ProductImportItem p WHERE p.importId = :importId")
    List<ProductImportItem> findByImportId(@Param("importId") UUID importId);

    @Query("SELECT p FROM ProductImportItem p WHERE p.importId = :importId AND p.status = 'REJECTED'")
    List<ProductImportItem> findRejectedByImportId(@Param("importId") UUID importId);

    @Query("SELECT COUNT(p) FROM ProductImportItem p WHERE p.importId = :importId AND p.status = :status")
    long countByImportIdAndStatus(@Param("importId") UUID importId, @Param("status") ImportItemStatus status);
}