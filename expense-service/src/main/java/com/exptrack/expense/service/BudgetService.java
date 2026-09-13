package com.exptrack.expense.service;

import com.exptrack.expense.domain.Budget;
import com.exptrack.expense.dto.*;
import com.exptrack.expense.events.BudgetExceededEvent;
import com.exptrack.expense.exceptions.BudgetAlreadyExistsException;
import com.exptrack.expense.exceptions.BudgetNotFoundException;
import com.exptrack.expense.repository.BudgetRepository;
import com.exptrack.expense.repository.BudgetSpecification;
import com.exptrack.expense.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepo;
    private final TransactionRepository transactionRepo;

    public BudgetService(BudgetRepository budgetRepo, TransactionRepository transactionRepo) {
        this.budgetRepo = budgetRepo;
        this.transactionRepo = transactionRepo;
    }

    /**
     * Lists budgets for a given user based on the provided filter and pagination information.
     * If no sorting is specified in the pageable, it defaults to sorting by year and month in descending order.
     *
     * @param userId   the ID of the user whose budgets are to be listed
     * @param filter   the filter criteria for listing budgets
     * @param pageable the pagination information
     * @return a page of BudgetResponse objects matching the filter criteria
     */
    public Page<BudgetResponse> list(UUID userId, BudgetFilter filter, Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "year")
                            .and(Sort.by(Sort.Direction.DESC, "month")));
        }
        return budgetRepo.findAll(
                        BudgetSpecification.filterBudget(userId, filter),
                        pageable)
                .map(this::toResponse);
    }

    /**
     * Finds a specific budget for a given user by budget ID.
     *
     * @param userId   the ID of the user
     * @param budgetId the ID of the budget to find
     * @return the BudgetResponse corresponding to the found budget
     * @throws BudgetNotFoundException if no budget is found for the given user and budget ID
     */
    public BudgetResponse find(UUID userId, UUID budgetId) {
        Budget b = budgetRepo.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new BudgetNotFoundException(budgetId, userId));
        return toResponse(b);
    }

    /**
     * Creates a new budget for a given user based on the provided budget request.
     *
     * @param userId  the ID of the user for whom the budget is to be created
     * @param request the budget request containing details of the budget to create
     * @return the BudgetResponse corresponding to the newly created budget
     * @throws BudgetAlreadyExistsException if a budget already exists for the given user, category, year, and month
     */
    public BudgetResponse create(UUID userId, BudgetRequest request) {

        if (budgetRepo.existsByUserIdAndCategoryIdAndYearAndMonth(
                userId, request.categoryId(), request.year(), request.month())) {
            throw new BudgetAlreadyExistsException();
        }

        Budget b = new Budget(
                userId, request.categoryId(), request.year(),
                request.month(), request.amount());

        budgetRepo.save(b);

        return toResponse(b);
    }

    /**
     * Updates an existing budget for a given user based on the provided budget request.
     *
     * @param userId   the ID of the user whose budget is to be updated
     * @param budgetId the ID of the budget to update
     * @param request  the budget request containing updated details of the budget
     * @return the BudgetResponse corresponding to the updated budget
     * @throws BudgetNotFoundException if no budget is found for the given user and budget ID
     */
    public BudgetResponse update(UUID userId, UUID budgetId, BudgetRequest request) {
        Budget b = budgetRepo.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new BudgetNotFoundException(budgetId, userId));

        b.setCategoryId(request.categoryId());
        b.setYear(request.year());
        b.setMonth(request.month());
        b.setAmount(request.amount());

        return toResponse(b);
    }

    /**
     * Deletes a specific budget for a given user by budget ID.
     *
     * @param userId   the ID of the user whose budget is to be deleted
     * @param budgetId the ID of the budget to delete
     * @throws BudgetNotFoundException if no budget is found for the given user and budget ID
     */
    public void delete(UUID userId, UUID budgetId) {
        Budget b = budgetRepo.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new BudgetNotFoundException(budgetId, userId));
        budgetRepo.delete(b);
    }

    public Optional<BudgetExceededEvent> checkBudget(UUID userId, UUID categoryId, LocalDate date) {

        YearMonth month = YearMonth.from(date);

        return budgetRepo.findByUserIdAndCategoryIdAndYearAndMonth(
                userId, categoryId, month.getYear(), month.getMonthValue())
                .map(budget -> new BudgetExceededEvent(
                        userId, categoryId, month.getYear(), month.getMonthValue(),
                        budget.getAmount(),
                        transactionRepo.sumExpenses(userId, categoryId,
                                month.atDay(1), month.plusMonths(1).atDay(1))))
                .filter(event -> event.actualAmount().compareTo(event.budgetAmount()) > 0);
    }

    /**
     * Converts a Budget entity to a BudgetResponse DTO.
     *
     * @param b the Budget entity to convert
     * @return the corresponding BudgetResponse DTO
     */
    private BudgetResponse toResponse(Budget b) {
        return new BudgetResponse(
                b.getId(), b.getCategoryId(),
                b.getYear(), b.getMonth(), b.getAmount());
    }
}
