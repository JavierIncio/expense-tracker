package com.exptrack.expense.repository;

import com.exptrack.expense.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID>, JpaSpecificationExecutor<Budget> {
    Optional<Budget> findByIdAndUserId(UUID id, UUID userId);

    List<Budget> findByUserIdAndYearAndMonth(UUID userId, Integer year, Integer month);

    Optional<Budget> findByUserIdAndCategoryIdAndYearAndMonth(
            UUID userId, UUID categoryId, Integer year, Integer month);

    boolean existsByUserIdAndCategoryIdAndYearAndMonth(
            UUID userId, UUID categoryId, Integer year, Integer month);
}
