package com.exptrack.expense.web;

import com.exptrack.expense.dto.TransactionFilter;
import com.exptrack.expense.dto.TransactionRequest;
import com.exptrack.expense.dto.TransactionResponse;
import com.exptrack.expense.security.UserPrincipal;
import com.exptrack.expense.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public TransactionResponse createTransaction(@AuthenticationPrincipal UserPrincipal user,
                                                 @Valid @RequestBody TransactionRequest request) {
        return transactionService.create(user.userId(), request);
    }

    @GetMapping
    public Page<TransactionResponse> listTransactions(@AuthenticationPrincipal UserPrincipal user,
                                                      @ModelAttribute TransactionFilter filter,
                                                      Pageable pageable) {
        return transactionService.list(user.userId(), filter, pageable);
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransaction(@AuthenticationPrincipal UserPrincipal user,
                                              @PathVariable UUID id) {
        return transactionService.find(user.userId(), id);
    }

    @PutMapping("/{id}")
    public TransactionResponse updateTransaction(@AuthenticationPrincipal UserPrincipal user,
                                                 @PathVariable UUID id,
                                                 @Valid @RequestBody TransactionRequest request) {
        return transactionService.update(user.userId(), id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteTransaction(@AuthenticationPrincipal UserPrincipal user,
                                  @PathVariable UUID id) {
        transactionService.delete(user.userId(), id);
    }
}
