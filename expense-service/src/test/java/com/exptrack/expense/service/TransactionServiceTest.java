package com.exptrack.expense.service;

import com.exptrack.expense.domain.Category;
import com.exptrack.expense.domain.Transaction;
import com.exptrack.expense.domain.TransactionType;
import com.exptrack.expense.dto.TransactionFilter;
import com.exptrack.expense.dto.TransactionRequest;
import com.exptrack.expense.dto.TransactionResponse;
import com.exptrack.expense.events.BudgetExceededEvent;
import com.exptrack.expense.events.DomainEventPublisher;
import com.exptrack.expense.exceptions.CategoryNotFoundException;
import com.exptrack.expense.exceptions.TransactionNotFoundException;
import com.exptrack.expense.exceptions.TransactionTypeMismatchException;
import com.exptrack.expense.repository.CategoryRepository;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepo;
    @Mock CategoryRepository categoryRepo;
    @Mock BudgetService budgetService;
    @Mock DomainEventPublisher eventPublisher;

    TransactionService service;

    final UUID userId = UUID.randomUUID();
    final UUID categoryId = UUID.randomUUID();
    final UUID transactionId = UUID.randomUUID();
    final LocalDate date = LocalDate.of(2026, 2, 10);
    final Category expenseCategory = new Category(userId, "Comida", TransactionType.EXPENSE);
    final Category incomeCategory = new Category(userId, "Salario", TransactionType.INCOME);

    @BeforeEach
    void setUp() {
        service = new TransactionService(transactionRepo, categoryRepo, budgetService, eventPublisher);
    }

    @Test
    void create_persistsTransactionWithDescription() {
        when(categoryRepo.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(expenseCategory));

        TransactionResponse response = service.create(userId, request(TransactionType.EXPENSE, "50.00", "almuerzo"));

        assertThat(response.description()).isEqualTo("almuerzo");
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepo).save(captor.capture());
        Transaction saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(saved.getCategoryId()).isEqualTo(categoryId);
        assertThat(saved.getAmount()).isEqualByComparingTo("50.00");
        assertThat(saved.getDescription()).isEqualTo("almuerzo");
    }

    @Test
    void create_expenseExceedingBudget_publishesEvent() {
        when(categoryRepo.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(expenseCategory));
        BudgetExceededEvent event = new BudgetExceededEvent(
                userId, categoryId, 2026, 2, new BigDecimal("100.00"), new BigDecimal("120.00"));
        when(budgetService.checkBudget(userId, categoryId, date)).thenReturn(Optional.of(event));

        service.create(userId, request(TransactionType.EXPENSE, "120.00", null));

        verify(eventPublisher).publishBudgetExceeded(event);
    }

    @Test
    void create_expenseWithinBudget_doesNotPublish() {
        when(categoryRepo.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(expenseCategory));
        when(budgetService.checkBudget(userId, categoryId, date)).thenReturn(Optional.empty());

        service.create(userId, request(TransactionType.EXPENSE, "80.00", null));

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void create_income_neverChecksBudget() {
        when(categoryRepo.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(incomeCategory));

        service.create(userId, request(TransactionType.INCOME, "100.00", null));

        verify(budgetService, never()).checkBudget(any(), any(), any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void create_typeMismatch_throws() {
        when(categoryRepo.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(expenseCategory));

        assertThatThrownBy(() -> service.create(userId, request(TransactionType.INCOME, "50.00", null)))
                .isInstanceOf(TransactionTypeMismatchException.class);
    }

    @Test
    void create_categoryNotFound_throws() {
        when(categoryRepo.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(userId, request(TransactionType.EXPENSE, "50.00", null)))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void update_returnsUpdatedResponse() {
        when(categoryRepo.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(expenseCategory));
        when(transactionRepo.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(new Transaction(userId, TransactionType.EXPENSE, new BigDecimal("30.00"), categoryId, date, null)));

        TransactionResponse response = service.update(userId, transactionId, request(TransactionType.EXPENSE, "60.00", "no me pagues"));

        assertThat(response.amount()).isEqualByComparingTo("60.00");
        assertThat(response.description()).isEqualTo("no me pagues");
    }

    @Test
    void update_expenseExceedingBudget_publishesEvent() {
        when(categoryRepo.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(expenseCategory));
        when(transactionRepo.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(new Transaction(userId, TransactionType.EXPENSE, new BigDecimal("30.00"), categoryId, date, null)));
        BudgetExceededEvent event = new BudgetExceededEvent(
                userId, categoryId, 2026, 2, new BigDecimal("100.00"), new BigDecimal("120.00"));
        when(budgetService.checkBudget(userId, categoryId, date)).thenReturn(Optional.of(event));

        service.update(userId, transactionId, request(TransactionType.EXPENSE, "60.00", null));

        verify(eventPublisher).publishBudgetExceeded(event);
    }

    @Test
    void update_transactionNotFound_throws() {
        when(categoryRepo.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(expenseCategory));
        when(transactionRepo.findByIdAndUserId(transactionId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(userId, transactionId, request(TransactionType.EXPENSE, "60.00", null)))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void find_returnsResponse() {
        when(transactionRepo.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(new Transaction(userId, TransactionType.EXPENSE, new BigDecimal("50.00"), categoryId, date, null)));

        TransactionResponse response = service.find(userId, transactionId);

        assertThat(response.amount()).isEqualByComparingTo("50.00");
    }

    @Test
    void find_notFound_throws() {
        when(transactionRepo.findByIdAndUserId(transactionId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.find(userId, transactionId))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void delete_removesTransaction() {
        Transaction t = new Transaction(userId, TransactionType.EXPENSE, new BigDecimal("50.00"), categoryId, date, null);
        when(transactionRepo.findByIdAndUserId(transactionId, userId)).thenReturn(Optional.of(t));

        service.delete(userId, transactionId);

        verify(transactionRepo).delete(t);
    }

    @Test
    void list_unsorted_appliesDateDescDefault() {
        when(transactionRepo.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.list(userId, new TransactionFilter(null, null, null, null), PageRequest.of(0, 20));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionRepo).findAll(any(Specification.class), captor.capture());
        assertThat(captor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "date"));
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void list_withSort_keepsGivenSort() {
        when(transactionRepo.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Pageable requested = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "amount"));
        service.list(userId, new TransactionFilter(null, null, null, null), requested);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionRepo).findAll(any(Specification.class), captor.capture());
        assertThat(captor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "amount"));
    }

    private TransactionRequest request(TransactionType type, String amount, String description) {
        return new TransactionRequest(type, description, new BigDecimal(amount), categoryId, date);
    }
}