package com.exptrack.expense.repository;

import com.exptrack.expense.domain.Transaction;
import com.exptrack.expense.dto.TransactionFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Provides a specification for filtering transactions based on various criteria.
 */
public class TransactionSpecification {

    /**
     * Filters transactions based on the provided TransactionFilter filter.
     *
     * <ul>
     *     <li>UserId: It filters transactions by the specified user ID.</li>
     *     <li>Type: If the type field is not null, it filters transactions by the specified type.</li>
     *     <li>Category: If the categoryId field is not null and not empty, it filters transactions by
     *     the specified category ID.</li>
     *     <li>Date Range:</li>
     *     <ul>
     *         <li>If fromDate is provided, it filters transactions from that date onwards.</li>
     *         <li>If toDate is provided, it filters transactions up to that date.</li>
     *     </ul>
     * </ul>
     *
     * @param filter The TransactionFilter containing filter criteria.
     * @return A Specification<Transaction> that can be used to query the database.
     */
    public static Specification<Transaction> filterTransaction(UUID userId, TransactionFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));

            if (filter.type() != null)
                predicates.add(criteriaBuilder.equal(
                        root.get("type"), filter.type()));

            if (filter.categoryId() != null)
                predicates.add(criteriaBuilder.equal(
                        root.get("categoryId"), filter.categoryId()));

            if (filter.fromDate() != null)
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("date"), filter.fromDate()));

            if (filter.toDate() != null)
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("date"), filter.toDate()));

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0]));
        };
    }
}
