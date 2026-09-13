package com.exptrack.expense.service;

import com.exptrack.expense.domain.Category;
import com.exptrack.expense.domain.Transaction;
import com.exptrack.expense.domain.TransactionType;
import com.exptrack.expense.dto.TransactionFilter;
import com.exptrack.expense.dto.TransactionRequest;
import com.exptrack.expense.dto.TransactionResponse;
import com.exptrack.expense.events.DomainEventPublisher;
import com.exptrack.expense.exceptions.CategoryNotFoundException;
import com.exptrack.expense.exceptions.TransactionNotFoundException;
import com.exptrack.expense.exceptions.TransactionTypeMismatchException;
import com.exptrack.expense.repository.CategoryRepository;
import com.exptrack.expense.repository.TransactionRepository;
import com.exptrack.expense.repository.TransactionSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepo;
    private final CategoryRepository categoryRepo;
    private final BudgetService budgetService;
    private final DomainEventPublisher eventPublisher;

    public TransactionService(TransactionRepository transactionRepo,
                              CategoryRepository categoryRepo,
                              BudgetService budgetService,
                              DomainEventPublisher eventPublisher) {
        this.transactionRepo = transactionRepo;
        this.categoryRepo = categoryRepo;
        this.budgetService = budgetService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Lists transactions for a given user based on the provided filter and pagination information.
     * If no sorting is specified in the pageable, it defaults to sorting by date in descending order.
     *
     * @param userId   the ID of the user whose transactions are to be listed
     * @param filter   the filter criteria for listing transactions
     * @param pageable the pagination information
     * @return a page of TransactionResponse objects matching the filter criteria
     */
    public Page<TransactionResponse> list(UUID userId, TransactionFilter filter, Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "date"));
        }
        return transactionRepo.findAll(
                        TransactionSpecification.filterTransaction(userId, filter),
                        pageable)
                .map(this::toResponse);
    }

    /**
     * Finds a specific transaction for a given user by transaction ID.
     *
     * @param userId        the ID of the user
     * @param transactionId the ID of the transaction to find
     * @return the TransactionResponse corresponding to the found transaction
     * @throws TransactionNotFoundException if no transaction is found for the given user and transaction ID
     */
    public TransactionResponse find(UUID userId, UUID transactionId) {
        Transaction t = transactionRepo.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId, userId));
        return toResponse(t);
    }

    /**
     * Creates a new transaction for a given user based on the provided transaction request.
     *
     * @param userId  the ID of the user for whom the transaction is to be created
     * @param request the transaction request containing details of the transaction to create
     * @return the TransactionResponse corresponding to the newly created transaction
     * @throws CategoryNotFoundException        if the specified category does not exist for the user
     * @throws TransactionTypeMismatchException if the transaction type does not match the category type
     */
    public TransactionResponse create(UUID userId, TransactionRequest request) {
        Category c = categoryRepo.findByIdAndUserId(request.categoryId(), userId)
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId(), userId));

        if (request.type() != c.getType()) throw new TransactionTypeMismatchException();

        String description = request.description();
        Transaction t = new Transaction(
                userId, request.type(), request.amount(), request.categoryId(), request.date(),
                description == null || description.isEmpty() ? null : description);

        transactionRepo.save(t);

        if (request.type() == TransactionType.EXPENSE) {
            budgetService.checkBudget(userId, request.categoryId(), request.date())
                    .ifPresent(eventPublisher::publishBudgetExceeded);
        }

        return toResponse(t);
    }

    /**
     * Updates an existing transaction for a given user based on the provided transaction request.
     *
     * @param userId        the ID of the user
     * @param transactionId the ID of the transaction to update
     * @param request       the transaction request containing updated details of the transaction
     * @return the TransactionResponse corresponding to the updated transaction
     * @throws CategoryNotFoundException        if the specified category does not exist for the user
     * @throws TransactionTypeMismatchException if the transaction type does not match the category type
     * @throws TransactionNotFoundException      if no transaction is found for the given user and transaction ID
     */
    public TransactionResponse update(UUID userId, UUID transactionId, TransactionRequest request) {
        Category c = categoryRepo.findByIdAndUserId(request.categoryId(), userId)
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId(), userId));

        if (request.type() != c.getType()) throw new TransactionTypeMismatchException();

        Transaction t = transactionRepo.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId, userId));

        t.setType(request.type());
        t.setAmount(request.amount());
        t.setCategoryId(request.categoryId());
        t.setDate(request.date());
        t.setDescription(request.description());

        if (request.type() == TransactionType.EXPENSE) {
            budgetService.checkBudget(userId, request.categoryId(), request.date())
                    .ifPresent(eventPublisher::publishBudgetExceeded);
        }

        return toResponse(t);
    }

    /**
     * Deletes a transaction for a given user by transaction ID.
     *
     * @param userId        the ID of the user
     * @param transactionId the ID of the transaction to delete
     * @throws TransactionNotFoundException if no transaction is found for the given user and transaction ID
     */
    public void delete(UUID userId, UUID transactionId) {
        Transaction t = transactionRepo.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId, userId));
        transactionRepo.delete(t);
    }

    /**
     * Converts a Transaction entity to a TransactionResponse DTO.
     *
     * @param t the Transaction entity to convert
     * @return the corresponding TransactionResponse DTO
     */
    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(), t.getType(),
                (t.getDescription() == null || t.getDescription().isEmpty())
                ? null
                : t.getDescription(),
                t.getAmount(), t.getCategoryId(),
                t.getDate(), t.getCreatedAt());
    }
}
