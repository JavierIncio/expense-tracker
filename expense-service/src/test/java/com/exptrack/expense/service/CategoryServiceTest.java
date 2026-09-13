package com.exptrack.expense.service;

import com.exptrack.expense.domain.Category;
import com.exptrack.expense.domain.TransactionType;
import com.exptrack.expense.dto.CategoryRequest;
import com.exptrack.expense.dto.CategoryResponse;
import com.exptrack.expense.exceptions.CategoryInUseException;
import com.exptrack.expense.exceptions.CategoryNotFoundException;
import com.exptrack.expense.repository.CategoryRepository;
import com.exptrack.expense.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepo;
    @Mock TransactionRepository transactionRepo;

    CategoryService service;

    final UUID userId = UUID.randomUUID();
    final UUID categoryId = UUID.randomUUID();
    final Category category = new Category(userId, "Comida", TransactionType.EXPENSE);

    @BeforeEach
    void setUp() {
        category.setId(categoryId);
        service = new CategoryService(categoryRepo, transactionRepo);
    }

    @Test
    void create_savesAndReturnsResponse() {
        CategoryResponse response = service.create(userId, new CategoryRequest("Comida", TransactionType.EXPENSE));

        assertThat(response.name()).isEqualTo("Comida");
        assertThat(response.type()).isEqualTo(TransactionType.EXPENSE);
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepo).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getName()).isEqualTo("Comida");
    }

    @Test
    void find_returnsResponse() {
        when(categoryRepo.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));

        CategoryResponse response = service.find(userId, categoryId);

        assertThat(response.id()).isEqualTo(categoryId);
        assertThat(response.name()).isEqualTo("Comida");
    }

    @Test
    void find_notFound_throws() {
        when(categoryRepo.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.find(userId, categoryId))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void update_mutatesCategory() {
        when(categoryRepo.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));

        CategoryResponse response = service.update(userId, categoryId, new CategoryRequest("Restaurante", TransactionType.EXPENSE));

        assertThat(response.name()).isEqualTo("Restaurante");
        verify(categoryRepo, never()).save(category);
    }

    @Test
    void update_notFound_throws() {
        when(categoryRepo.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(userId, categoryId, new CategoryRequest("Restaurante", TransactionType.EXPENSE)))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void list_withoutType_usesFindByUserId() {
        when(categoryRepo.findByUserId(userId)).thenReturn(List.of(category));

        List<CategoryResponse> categories = service.list(userId, null);

        assertThat(categories).hasSize(1);
        verify(categoryRepo).findByUserId(userId);
    }

    @Test
    void list_withType_usesFindByUserIdAndType() {
        when(categoryRepo.findByUserIdAndType(userId, TransactionType.EXPENSE)).thenReturn(List.of(category));

        List<CategoryResponse> categories = service.list(userId, TransactionType.EXPENSE);

        assertThat(categories).hasSize(1);
        verify(categoryRepo).findByUserIdAndType(userId, TransactionType.EXPENSE);
    }

    @Test
    void delete_inUse_throws() {
        when(categoryRepo.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));
        when(transactionRepo.existsByCategoryId(categoryId)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(userId, categoryId))
                .isInstanceOf(CategoryInUseException.class);
        verify(categoryRepo, never()).delete(category);
    }

    @Test
    void delete_ok_removesCategory() {
        when(categoryRepo.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));
        when(transactionRepo.existsByCategoryId(categoryId)).thenReturn(false);

        service.delete(userId, categoryId);

        verify(categoryRepo).delete(category);
    }
}