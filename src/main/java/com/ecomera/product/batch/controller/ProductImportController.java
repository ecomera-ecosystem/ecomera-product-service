package com.ecomera.product.batch.controller;

import com.ecomera.product.batch.dto.ProductImportLaunchResponse;
import com.ecomera.product.batch.dto.ProductImportResponse;
import com.ecomera.product.batch.service.ProductImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/imports")
@Tag(name = "Product Imports", description = "Admin APIs for product CSV imports")
public class ProductImportController {

    private final ProductImportService importService;

    private static final String X_USER_ID = "X-User-Id";

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Upload CSV and launch product import", description = "Upload a CSV file and launch an asynchronous import job")
    @ApiResponse(responseCode = "202", description = "Import job launched successfully")
    @ApiResponse(responseCode = "400", description = "Invalid file or request")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<ProductImportLaunchResponse> launchImport(
            @Parameter(description = "CSV file to import", required = true)
            @RequestPart("file") @Valid MultipartFile file,
            @RequestHeader(value = X_USER_ID, required = false) String userId) throws Exception {
        
        String initiatedBy = (userId != null && !userId.isBlank()) ? userId : "anonymous";
        ProductImportLaunchResponse response = importService.launchImport(file, initiatedBy);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{importId}")
    @Operation(summary = "Get import status", description = "Get the status and results of an import job")
    @ApiResponse(responseCode = "200", description = "Import status retrieved")
    @ApiResponse(responseCode = "404", description = "Import not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<ProductImportResponse> getImportStatus(
            @Parameter(description = "Import ID") @PathVariable UUID importId) {
        
        return importService.getImportStatus(importId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List my imports", description = "Get paginated list of imports")
    @ApiResponse(responseCode = "200", description = "Imports retrieved")
    public ResponseEntity<org.springframework.data.domain.Page<ProductImportResponse>> getMyImports(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        return ResponseEntity.ok(importService.getMyImports(page, size));
    }

    @GetMapping(value = "/{importId}/errors", produces = "text/csv")
    @Operation(summary = "Download rejected rows CSV", description = "Download a CSV file containing rejected rows and error details")
    @ApiResponse(responseCode = "200", description = "Error CSV downloaded")
    @ApiResponse(responseCode = "404", description = "Import not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<org.springframework.core.io.Resource> downloadErrors(@PathVariable UUID importId) throws IOException {
        String csv = "line_number,sku,title,description,price,stock,category_id,color,size,error_message\n";
        ByteArrayInputStream bis = new ByteArrayInputStream(csv.getBytes());
        InputStreamResource resource = new InputStreamResource(bis);
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=import-errors-" + importId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
}