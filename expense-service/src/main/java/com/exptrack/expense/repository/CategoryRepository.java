package com.exptrack.expense.repository;

import com.exptrack.expense.domain.Category;
import com.exptrack.expense.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByIdAndUserId(UUID categoryId, UUID userId);
    List<Category> findByUserId(UUID userId);
    List<Category> findByUserIdAndType(UUID userId, TransactionType type);
}
