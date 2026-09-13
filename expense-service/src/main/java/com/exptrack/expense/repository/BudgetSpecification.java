package com.exptrack.expense.repository;

import com.exptrack.expense.domain.Budget;
import com.exptrack.expense.dto.BudgetFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Provides a specification for filtering budgets based on various criteria.
 */
public class BudgetSpecification {

    /**
     * Filters budgets based on the provided BudgetFilter fields.
     *
     * <ul>
     *     <li>UserId: It filters budgets by the specified user ID.</li>
     *     <li>Category: If the categoryId field is not null, it filters budgets by the specified category ID.</li>
     *     <li>Year: If the year field is not null, it filters budgets by the specified year.</li>
     *     <li>Month: If the month field is not null, it filters budgets by the specified month.</li>
     * </ul>
     *
     * @param userId The UUID of the user to filter budgets for.
     * @param filter The BudgetFilter containing filter criteria.
     * @return A Specification<Budget> that can be used to query the database.
     */
    public static Specification<Budget> filterBudget(UUID userId, BudgetFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));

            if (filter.categoryId() != null)
                predicates.add(criteriaBuilder.equal(
                        root.get("categoryId"), filter.categoryId()));

            if (filter.year() != null)
                predicates.add(criteriaBuilder.equal(
                        root.get("year"), filter.year()));

            if (filter.month() != null)
                predicates.add(criteriaBuilder.equal(
                        root.get("month"), filter.month()));

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0]));
        };
    }
}
