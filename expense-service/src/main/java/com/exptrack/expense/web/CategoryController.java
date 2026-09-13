package com.exptrack.expense.web;

import com.exptrack.expense.domain.TransactionType;
import com.exptrack.expense.dto.*;
import com.exptrack.expense.security.UserPrincipal;
import com.exptrack.expense.service.CategoryService;
import jakarta.validation.Valid;
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
    public CategoryResponse createCategory(@AuthenticationPrincipal UserPrincipal user,
                                           @Valid @RequestBody CategoryRequest request) {
        return categoryService.create(user.userId(), request);
    }

    @GetMapping
    public List<CategoryResponse> listCategories(@AuthenticationPrincipal UserPrincipal user,
                                                 @RequestParam(required = false) TransactionType type) {
        return categoryService.list(user.userId(), type);
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategory(@AuthenticationPrincipal UserPrincipal user,
                                       @PathVariable UUID id) {
        return categoryService.find(user.userId(), id);
    }

    @PutMapping("/{id}")
    public CategoryResponse updateCategory(@AuthenticationPrincipal UserPrincipal user,
                                                 @PathVariable UUID id,
                                                 @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(user.userId(), id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@AuthenticationPrincipal UserPrincipal user,
                                  @PathVariable UUID id) {
        categoryService.delete(user.userId(), id);
    }
}
