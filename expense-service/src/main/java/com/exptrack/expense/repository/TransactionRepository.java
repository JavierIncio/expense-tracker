package com.exptrack.expense.repository;

import com.exptrack.expense.domain.Transaction;
import com.exptrack.expense.dto.CategoryTotal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
    // Redefined for learning purposes, but JpaSpecificationExecutor already provides this method
    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByCategoryId(UUID categoryId);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.userId = :userId AND t.categoryId = :categoryId
                  AND t.type = com.exptrack.expense.domain.TransactionType.EXPENSE
                  AND t.date >= :fromDate AND t.date < :toDate
            """)
    BigDecimal sumExpenses(UUID userId, UUID categoryId, LocalDate fromDate, LocalDate toDate);

    @Query("""
            SELECT t.categoryId AS categoryId, t.type AS type, SUM(t.amount) AS total
            FROM Transaction t
            WHERE t.userId = :userId AND t.date >= :fromDate AND t.date < :toDate
            GROUP BY t.categoryId, t.type
""")
    List<CategoryTotal> sumByCategory(UUID userId, LocalDate fromDate, LocalDate toDate);

}
