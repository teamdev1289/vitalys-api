package com.vitalys.modules.product.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.product.dto.BatchResponse;
import com.vitalys.modules.product.dto.ProductCreateRequest;
import com.vitalys.modules.product.dto.ProductResponse;
import com.vitalys.modules.product.dto.ProductUpdateRequest;
import com.vitalys.modules.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller providing REST API for pharmaceutical product catalog.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "CRUD operations for drug products and formulations")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "List products with search, status filtering, and pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT:MASTER:READ')")
    public ResponseEntity<ResponseDto<Page<ProductResponse>>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                ResponseDto.ok(productService.getProducts(search, status, pageable)));
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT:MASTER:READ')")
    public ResponseEntity<ResponseDto<ProductResponse>> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(productService.getProductById(id)));
    }

    @Operation(summary = "Get all production batches for a product")
    @GetMapping("/{id}/batches")
    @PreAuthorize("hasAuthority('PRODUCT:BATCH:READ')")
    public ResponseEntity<ResponseDto<List<BatchResponse>>> getProductBatches(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(productService.getBatchesByProduct(id)));
    }

    @Operation(summary = "Register a new pharmaceutical product")
    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT:MASTER:CREATE')")
    public ResponseEntity<ResponseDto<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.created(response));
    }

    @Operation(summary = "Update product registration and specifications")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT:MASTER:UPDATE')")
    public ResponseEntity<ResponseDto<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(
                ResponseDto.ok("Product updated successfully", productService.updateProduct(id, request)));
    }

    @Operation(summary = "Delete product (only if no existing batches)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT:MASTER:DELETE')")
    public ResponseEntity<ResponseDto<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ResponseDto.noContent("Product deleted successfully"));
    }
}
