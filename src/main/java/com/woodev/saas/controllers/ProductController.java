package com.woodev.saas.controllers;

import com.woodev.saas.common.PageResponse;
import com.woodev.saas.requests.ProductRequest;
import com.woodev.saas.responses.ProductResponse;
import com.woodev.saas.services.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Void> createProduct(
            @RequestBody
            @Valid
            final ProductRequest productRequest){
        this.productService.create(productRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{product-id}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable("product-id")
            @NotNull(message = "Product ID cannot be null")
            final String id,
            @RequestBody
            @Valid
            final ProductRequest productRequest){
        this.productService.update(id, productRequest);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{product-id}")
    public ResponseEntity<ProductResponse> findProductById(
            @PathVariable("product-id")
            @NotNull(message = "Product ID cannot be null")
            final String id){
        return ResponseEntity.ok(this.productService.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> findAllProducts(
            @RequestParam(name="page", defaultValue = "0") final int page,
            @RequestParam(name="size", defaultValue = "10") final int size
    ) {
        return ResponseEntity.ok(this.productService.findAll(page, size));
    }

    @DeleteMapping("/{product-id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("product-id")
            final String id){
        this.productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
