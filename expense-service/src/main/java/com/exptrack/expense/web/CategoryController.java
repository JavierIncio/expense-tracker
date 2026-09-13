package com.exptrack.expense.web;

import com.exptrack.expense.domain.TransactionType;
import com.exptrack.expense.dto.*;
import com.exptrack.expense.security.UserPrincipal;
import com.exptrack.expense.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {this.categoryService = categoryService;}

@PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@AuthenticationPrincipal UserPrincipal user,
                                                           @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(user.userId(), request));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> listCategories(@AuthenticationPrincipal UserPrincipal user,
                                                                 @RequestParam(required = false) TransactionType type) {
        return ResponseEntity.ok(categoryService.list(user.userId(), type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@AuthenticationPrincipal UserPrincipal user,
                                                        @PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.find(user.userId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@AuthenticationPrincipal UserPrincipal user,
                                                           @PathVariable UUID id,
                                                           @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(user.userId(), id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@AuthenticationPrincipal UserPrincipal user,
                               @PathVariable UUID id) {
        categoryService.delete(user.userId(), id);
    }
}
