package com.exptrack.expense.service;

import com.exptrack.expense.domain.Category;
import com.exptrack.expense.domain.TransactionType;
import com.exptrack.expense.dto.CategoryRequest;
import com.exptrack.expense.dto.CategoryResponse;
import com.exptrack.expense.exceptions.CategoryInUseException;
import com.exptrack.expense.exceptions.CategoryNotFoundException;
import com.exptrack.expense.repository.CategoryRepository;
import com.exptrack.expense.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepo;
    private final TransactionRepository transactionRepo;

    public CategoryService(CategoryRepository categoryRepo, TransactionRepository transactionRepo) {
        this.categoryRepo = categoryRepo;
        this.transactionRepo = transactionRepo;
    }

    /**
     * Lists all categories for a specific user, optionally filtered by transaction type.
     *
     * @param userId The UUID of the user whose categories are to be listed.
     * @param type   An optional TransactionType to filter the categories. If null, all categories are returned.
     * @return A list of CategoryResponse DTOs representing the user's categories.
     */
    public List<CategoryResponse> list(UUID userId, TransactionType type) {
        List<Category> categories;
        if (type != null) {
            categories = categoryRepo.findByUserIdAndType(userId, type);
        } else {
            categories = categoryRepo.findByUserId(userId);
        }
        return categories.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Finds a specific category for a user by category ID.
     *
     * @param userId     The UUID of the user.
     * @param categoryId The UUID of the category to find.
     * @return A CategoryResponse DTO representing the found category.
     * @throws CategoryNotFoundException if the category is not found for the given user.
     */
    public CategoryResponse find(UUID userId, UUID categoryId) {
        Category category = categoryRepo.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId, userId));

        return toResponse(category);
    }

    /**
     * Creates a new category for a user based on the provided request.
     *
     * @param userId  The UUID of the user for whom the category is to be created.
     * @param request The CategoryRequest DTO containing the details of the category to create.
     * @return A CategoryResponse DTO representing the newly created category.
     */
    public CategoryResponse create(UUID userId, CategoryRequest request) {
        Category category = new Category(userId, request.name(), request.type());
        categoryRepo.save(category);
        return toResponse(category);
    }

    /**
     * Updates an existing category for a user based on the provided request.
     *
     * @param userId     The UUID of the user who owns the category.
     * @param categoryId The UUID of the category to update.
     * @param request    The CategoryRequest DTO containing the updated details of the category.
     * @return A CategoryResponse DTO representing the updated category.
     * @throws CategoryNotFoundException if the category is not found for the given user.
     */
    public CategoryResponse update(UUID userId, UUID categoryId, CategoryRequest request) {
        Category category = categoryRepo.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId, userId));

        category.setName(request.name());
        category.setType(request.type());

        return toResponse(category);
    }

    /**
     * Deletes a category for a user if it is not in use by any transactions.
     *
     * @param userId     The UUID of the user who owns the category.
     * @param categoryId The UUID of the category to delete.
     * @throws CategoryNotFoundException if the category is not found for the given user.
     * @throws CategoryInUseException    if the category is currently in use by any transactions.
     */
    public void delete(UUID userId, UUID categoryId) {
        Category category = categoryRepo.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId, userId));

        if (transactionRepo.existsByCategoryId(categoryId))
            throw new CategoryInUseException(categoryId);

        categoryRepo.delete(category);
    }

    /**
     * Converts a Category entity to a CategoryResponse DTO.
     *
     * @param category The Category entity to convert.
     * @return The corresponding CategoryResponse DTO.
     */
    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
