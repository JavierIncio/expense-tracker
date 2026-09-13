package com.exptrack.expense.service;

import com.exptrack.expense.domain.Budget;
import com.exptrack.expense.dto.BudgetFilter;
import com.exptrack.expense.dto.BudgetRequest;
import com.exptrack.expense.dto.BudgetResponse;
import com.exptrack.expense.events.BudgetExceededEvent;
import com.exptrack.expense.exceptions.BudgetAlreadyExistsException;
import com.exptrack.expense.exceptions.BudgetNotFoundException;
import com.exptrack.expense.repository.BudgetRepository;
import com.exptrack.expense.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock BudgetRepository budgetRepo;
    @Mock TransactionRepository transactionRepo;

    BudgetService service;

    final UUID userId = UUID.randomUUID();
    final UUID categoryId = UUID.randomUUID();
    final UUID budgetId = UUID.randomUUID();
    final LocalDate date = LocalDate.of(2026, 2, 10);
    final Budget budget = new Budget(userId, categoryId, 2026, 2, new BigDecimal("100.00"));

    @BeforeEach
    void setUp() {
        service = new BudgetService(budgetRepo, transactionRepo);
    }

    @Test
    void create_duplicate_throws() {
        when(budgetRepo.existsByUserIdAndCategoryIdAndYearAndMonth(userId, categoryId, 2026, 2)).thenReturn(true);

        assertThatThrownBy(() -> service.create(userId, request("120.00")))
                .isInstanceOf(BudgetAlreadyExistsException.class);
        verify(budgetRepo, never()).save(any());
    }

    @Test
    void create_ok_savesBudget() {
        when(budgetRepo.existsByUserIdAndCategoryIdAndYearAndMonth(userId, categoryId, 2026, 2)).thenReturn(false);

        BudgetResponse response = service.create(userId, request("120.00"));

        assertThat(response.amount()).isEqualByComparingTo("120.00");
        assertThat(response.categoryId()).isEqualTo(categoryId);
        assertThat(response.year()).isEqualTo(2026);
        assertThat(response.month()).isEqualTo(2);
        ArgumentCaptor<Budget> captor = ArgumentCaptor.forClass(Budget.class);
        verify(budgetRepo).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
    }

    @Test
    void find_returnsResponse() {
        when(budgetRepo.findByIdAndUserId(budgetId, userId)).thenReturn(Optional.of(budget));

        BudgetResponse response = service.find(userId, budgetId);

        assertThat(response.amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void find_notFound_throws() {
        when(budgetRepo.findByIdAndUserId(budgetId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.find(userId, budgetId))
                .isInstanceOf(BudgetNotFoundException.class);
    }

    @Test
    void update_mutatesBudget() {
        when(budgetRepo.findByIdAndUserId(budgetId, userId)).thenReturn(Optional.of(budget));

        BudgetResponse response = service.update(userId, budgetId, request("140.00"));

        assertThat(response.amount()).isEqualByComparingTo("140.00");
        verify(budgetRepo, never()).save(any());
    }

    @Test
    void update_notFound_throws() {
        when(budgetRepo.findByIdAndUserId(budgetId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(userId, budgetId, request("140.00")))
                .isInstanceOf(BudgetNotFoundException.class);
    }

    @Test
    void delete_removesBudget() {
        when(budgetRepo.findByIdAndUserId(budgetId, userId)).thenReturn(Optional.of(budget));

        service.delete(userId, budgetId);

        verify(budgetRepo).delete(budget);
    }

    @Test
    void delete_notFound_throws() {
        when(budgetRepo.findByIdAndUserId(budgetId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(userId, budgetId))
                .isInstanceOf(BudgetNotFoundException.class);
    }

    @Test
    void list_unsorted_appliesYearMonthDescDefault() {
        when(budgetRepo.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.list(userId, new BudgetFilter(null, null, null), PageRequest.of(0, 20));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(budgetRepo).findAll(any(Specification.class), captor.capture());
        assertThat(captor.getValue().getSort())
                .isEqualTo(Sort.by(Sort.Direction.DESC, "year").and(Sort.by(Sort.Direction.DESC, "month")));
    }

    @Test
    void checkBudget_noBudget_returnsEmpty() {
        when(budgetRepo.findByUserIdAndCategoryIdAndYearAndMonth(userId, categoryId, 2026, 2))
                .thenReturn(Optional.empty());

        Optional<BudgetExceededEvent> result = service.checkBudget(userId, categoryId, date);

        assertThat(result).isEmpty();
    }

    @Test
    void checkBudget_overBudget_returnsEvent() {
        when(budgetRepo.findByUserIdAndCategoryIdAndYearAndMonth(userId, categoryId, 2026, 2))
                .thenReturn(Optional.of(budget));
        when(transactionRepo.sumExpenses(userId, categoryId, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1)))
                .thenReturn(new BigDecimal("120.00"));

        Optional<BudgetExceededEvent> result = service.checkBudget(userId, categoryId, date);

        assertThat(result).isPresent();
        BudgetExceededEvent event = result.get();
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.categoryId()).isEqualTo(categoryId);
        assertThat(event.year()).isEqualTo(2026);
        assertThat(event.month()).isEqualTo(2);
        assertThat(event.budgetAmount()).isEqualByComparingTo("100.00");
        assertThat(event.actualAmount()).isEqualByComparingTo("120.00");
    }

    @Test
    void checkBudget_atLimit_returnsEmpty() {
        when(budgetRepo.findByUserIdAndCategoryIdAndYearAndMonth(userId, categoryId, 2026, 2))
                .thenReturn(Optional.of(budget));
        when(transactionRepo.sumExpenses(userId, categoryId, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1)))
                .thenReturn(new BigDecimal("100.00"));

        Optional<BudgetExceededEvent> result = service.checkBudget(userId, categoryId, date);

        assertThat(result).isEmpty();
    }

    private BudgetRequest request(String amount) {
        return new BudgetRequest(categoryId, 2026, 2, new BigDecimal(amount));
    }
}