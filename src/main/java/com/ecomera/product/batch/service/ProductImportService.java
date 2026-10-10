package com.ecomera.product.batch.service;

import com.ecomera.product.batch.dto.ProductImportLaunchResponse;
import com.ecomera.product.batch.dto.ProductImportResponse;
import com.ecomera.product.batch.model.ImportStatus;
import com.ecomera.product.batch.model.ProductImport;
import com.ecomera.product.batch.repository.ProductImportRepository;
import com.ecomera.product.batch.repository.ProductImportItemRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductImportService {

    private final JobLauncher jobLauncher;
    private final Job productImportJob;
    private final ProductImportRepository importRepository;
    private final ProductImportItemRepository itemRepository;

    @Transactional
    public ProductImportLaunchResponse launchImport(MultipartFile file, String initiatedBy) throws Exception {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (!file.getContentType().equals("text/csv")) {
            throw new IllegalArgumentException("File must be CSV format");
        }

        // Calculate checksum
        String checksum = calculateChecksum(file);

        // Save file to temporary location
        Path tempFile = Files.createTempFile("import-", ".csv");
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

        // Create import record
        ProductImport importRecord = new ProductImport();
        importRecord.setFilename(file.getOriginalFilename());
        importRecord.setFileChecksum(checksum);
        importRecord.setInitiatedBy(initiatedBy);
        importRecord.setStatus(ImportStatus.PENDING);
        importRecord.setCreatedCount(0);
        importRecord.setUpdatedCount(0);
        importRecord.setRejectedCount(0);
        importRecord = importRepository.save(importRecord);

        // Launch job asynchronously
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("importId", importRecord.getId().toString())
                .addString("csvFile", file.getOriginalFilename())
                .addString("initiatedBy", initiatedBy)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(productImportJob, jobParameters);

        // Update import record with job execution info
        importRecord.setStatus(ImportStatus.RUNNING);
        importRecord.setStartedAt(LocalDateTime.now());
        importRepository.save(importRecord);

        return ProductImportLaunchResponse.builder()
                .importId(importRecord.getId())
                .statusUrl("/api/v1/admin/imports/" + importRecord.getId())
                .launchedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<ProductImportResponse> getImportStatus(UUID importId) {
        return importRepository.findById(importId)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductImportResponse> getMyImports(int page, int size) {
        return importRepository.findAll(
                PageRequest.of(page, size)
        ).map(this::mapToResponse);
    }

    private ProductImportResponse mapToResponse(ProductImport imp) {
        return ProductImportResponse.builder()
                .importId(imp.getId())
                .filename(imp.getFilename())
                .status(imp.getStatus().name())
                .totalRows(imp.getTotalRows())
                .createdCount(imp.getCreatedCount())
                .updatedCount(imp.getUpdatedCount())
                .rejectedCount(imp.getRejectedCount())
                .errorMessage(imp.getErrorMessage())
                .startedAt(imp.getStartedAt())
                .completedAt(imp.getCompletedAt())
                .build();
    }

    private String calculateChecksum(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }
}