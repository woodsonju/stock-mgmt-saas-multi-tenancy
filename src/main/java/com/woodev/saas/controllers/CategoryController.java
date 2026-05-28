package com.woodev.saas.controllers;

import com.woodev.saas.common.PageResponse;
import com.woodev.saas.requests.CategoryRequest;
import com.woodev.saas.responses.CategoryResponse;
import com.woodev.saas.services.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Void> createCategory(
            @RequestBody
            @Valid
            final CategoryRequest categoryRequest) {
        this.categoryService.create(categoryRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{category-id}")
    public ResponseEntity<Void> updateCategory(
            @PathVariable("category-id")
            @NotNull(message = "Category ID cannot be null")
            final String id,
            @RequestBody
            @Valid
            final CategoryRequest categoryRequest) {
        this.categoryService.update(id, categoryRequest);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{category-id}")
    public ResponseEntity<CategoryResponse> findCategoryById(
            @PathVariable("category-id")
            @NotNull(message = "Category ID cannot be null")
            final String id) {
        return ResponseEntity.ok(this.categoryService.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CategoryResponse>> findAllCategories(
            @RequestParam(name = "page", defaultValue = "0") final int page,
            @RequestParam(name = "size", defaultValue = "10") final int size) {
        return ResponseEntity.ok(this.categoryService.findAll(page, size));
    }

    @DeleteMapping("/{category-id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable("category-id")
            @NotNull(message = "Category ID cannot be null")
            final String id) {
        this.categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
