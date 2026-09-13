package com.exptrack.expense.service;

import com.exptrack.expense.domain.Budget;
import com.exptrack.expense.domain.BudgetStatus;
import com.exptrack.expense.domain.Category;
import com.exptrack.expense.domain.TransactionType;
import com.exptrack.expense.dto.CategorySummary;
import com.exptrack.expense.dto.CategoryTotal;
import com.exptrack.expense.dto.MonthlySummaryResponse;
import com.exptrack.expense.repository.BudgetRepository;
import com.exptrack.expense.repository.CategoryRepository;
import com.exptrack.expense.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SummaryService {

    private final TransactionRepository transactionRepo;
    private final CategoryRepository categoryRepo;
    private final BudgetRepository budgetRepo;

    public SummaryService(TransactionRepository transactionRepo,
                          CategoryRepository categoryRepo,
                          BudgetRepository budgetRepo) {
        this.transactionRepo = transactionRepo;
        this.categoryRepo = categoryRepo;
        this.budgetRepo = budgetRepo;
    }

    /**
     * Generates a monthly summary for a given user, year, and month.
     *
     * @param userId the ID of the user
     * @param year   the year for which the summary is generated
     * @param month  the month for which the summary is generated
     * @return a MonthlySummaryResponse containing income, expense, balance, and category-wise summaries
     */
    public MonthlySummaryResponse monthlySummary(UUID userId, int year, int month) {
        LocalDate from = YearMonth.of(year, month).atDay(1);
        LocalDate to = YearMonth.of(year, month).plusMonths(1).atDay(1);

        List<CategoryTotal> totals = transactionRepo.sumByCategory(userId, from, to);
        Map<UUID, String> categoryNames = categoryRepo.findByUserId(userId).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        Map<UUID, Budget> budgets = budgetRepo.findByUserIdAndYearAndMonth(userId, year, month).stream()
                .collect(Collectors.toMap(Budget::getCategoryId, b -> b));

        List<CategorySummary> byCategory = totals.stream().map(t -> {
            BigDecimal budget = budgets.containsKey(t.getCategoryId())
                                ? budgets.get(t.getCategoryId()).getAmount()
                                : null;

            BudgetStatus status = (budget == null)
                                  ? BudgetStatus.NO_BUDGET
                                  : t.getTotal().compareTo(budget) > 0
                                    ? BudgetStatus.EXCEEDED
                                    : BudgetStatus.WITHIN_LIMIT;

            return new CategorySummary(
                    t.getCategoryId(), categoryNames.get(t.getCategoryId()),
                    t.getType(), t.getTotal(), budget, status);
        }).toList();

        BigDecimal income = byCategory.stream()
                .filter(c -> c.type() == TransactionType.INCOME)
                .map(CategorySummary::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expense = byCategory.stream()
                .filter(c -> c.type() == TransactionType.EXPENSE)
                .map(CategorySummary::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlySummaryResponse(year, month, income, expense, income.subtract(expense), byCategory);
    }
}
