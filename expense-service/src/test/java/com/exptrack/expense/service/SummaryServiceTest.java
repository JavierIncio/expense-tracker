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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

    @Mock TransactionRepository transactionRepo;
    @Mock CategoryRepository categoryRepo;
    @Mock BudgetRepository budgetRepo;

    SummaryService service;

    final UUID userId = UUID.randomUUID();
    final UUID expenseId = UUID.randomUUID();
    final UUID incomeId = UUID.randomUUID();
    final Category comida = new Category(userId, "Comida", TransactionType.EXPENSE);
    final Category salario = new Category(userId, "Salario", TransactionType.INCOME);

    @BeforeEach
    void setUp() {
        comida.setId(expenseId);
        salario.setId(incomeId);
        service = new SummaryService(transactionRepo, categoryRepo, budgetRepo);
    }

    @Test
    void monthlySummary_aggregatesAndMarksStatuses() {
        CategoryTotal expenseTotal = total(expenseId, TransactionType.EXPENSE, "120.00");
        CategoryTotal incomeTotal = total(incomeId, TransactionType.INCOME, "50.00");
        when(transactionRepo.sumByCategory(userId, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1)))
                .thenReturn(List.of(expenseTotal, incomeTotal));
        when(categoryRepo.findByUserId(userId)).thenReturn(List.of(comida, salario));
        when(budgetRepo.findByUserIdAndYearAndMonth(userId, 2026, 2))
                .thenReturn(List.of(new Budget(userId, expenseId, 2026, 2, new BigDecimal("100.00"))));

        MonthlySummaryResponse summary = service.monthlySummary(userId, 2026, 2);

        assertThat(summary.year()).isEqualTo(2026);
        assertThat(summary.month()).isEqualTo(2);
        assertThat(summary.totalIncome()).isEqualByComparingTo("50.00");
        assertThat(summary.totalExpense()).isEqualByComparingTo("120.00");
        assertThat(summary.balance()).isEqualByComparingTo("-70.00");
        assertThat(summary.byCategory()).hasSize(2);

        CategorySummary comidaSummary = category(summary, expenseId);
        assertThat(comidaSummary.categoryName()).isEqualTo("Comida");
        assertThat(comidaSummary.amount()).isEqualByComparingTo("120.00");
        assertThat(comidaSummary.budgetAmount()).isEqualByComparingTo("100.00");
        assertThat(comidaSummary.budgetStatus()).isEqualTo(BudgetStatus.EXCEEDED);

        CategorySummary salarioSummary = category(summary, incomeId);
        assertThat(salarioSummary.categoryName()).isEqualTo("Salario");
        assertThat(salarioSummary.budgetAmount()).isNull();
        assertThat(salarioSummary.budgetStatus()).isEqualTo(BudgetStatus.NO_BUDGET);
    }

    @Test
    void monthlySummary_withinLimit_marksWithinLimit() {
        CategoryTotal expenseTotal = total(expenseId, TransactionType.EXPENSE, "80.00");
        when(transactionRepo.sumByCategory(userId, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1)))
                .thenReturn(List.of(expenseTotal));
        when(categoryRepo.findByUserId(userId)).thenReturn(List.of(comida));
        when(budgetRepo.findByUserIdAndYearAndMonth(userId, 2026, 2))
                .thenReturn(List.of(new Budget(userId, expenseId, 2026, 2, new BigDecimal("100.00"))));

        MonthlySummaryResponse summary = service.monthlySummary(userId, 2026, 2);

        assertThat(category(summary, expenseId).budgetStatus()).isEqualTo(BudgetStatus.WITHIN_LIMIT);
        assertThat(summary.totalExpense()).isEqualByComparingTo("80.00");
    }

    @Test
    void monthlySummary_noBudget_marksNoBudget() {
        CategoryTotal expenseTotal = total(expenseId, TransactionType.EXPENSE, "80.00");
        when(transactionRepo.sumByCategory(userId, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1)))
                .thenReturn(List.of(expenseTotal));
        when(categoryRepo.findByUserId(userId)).thenReturn(List.of(comida));
        when(budgetRepo.findByUserIdAndYearAndMonth(userId, 2026, 2)).thenReturn(List.of());

        MonthlySummaryResponse summary = service.monthlySummary(userId, 2026, 2);

        assertThat(category(summary, expenseId).budgetStatus()).isEqualTo(BudgetStatus.NO_BUDGET);
    }

    @Test
    void monthlySummary_noData_returnsZeroTotals() {
        when(transactionRepo.sumByCategory(userId, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1)))
                .thenReturn(List.of());
        when(categoryRepo.findByUserId(userId)).thenReturn(List.of());
        when(budgetRepo.findByUserIdAndYearAndMonth(userId, 2026, 2)).thenReturn(List.of());

        MonthlySummaryResponse summary = service.monthlySummary(userId, 2026, 2);

        assertThat(summary.totalIncome()).isZero();
        assertThat(summary.totalExpense()).isZero();
        assertThat(summary.balance()).isZero();
        assertThat(summary.byCategory()).isEmpty();
        verify(transactionRepo).sumByCategory(userId, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1));
    }

    private CategoryTotal total(UUID categoryId, TransactionType type, String amount) {
        CategoryTotal t = mock(CategoryTotal.class);
        when(t.getCategoryId()).thenReturn(categoryId);
        when(t.getType()).thenReturn(type);
        when(t.getTotal()).thenReturn(new BigDecimal(amount));
        return t;
    }

    private static CategorySummary category(MonthlySummaryResponse summary, UUID categoryId) {
        return summary.byCategory().stream()
                .filter(c -> c.categoryId().equals(categoryId))
                .findFirst()
                .orElseThrow();
    }
}