package com.exptrack.expense.web;

import com.exptrack.expense.dto.TransactionFilter;
import com.exptrack.expense.dto.TransactionRequest;
import com.exptrack.expense.dto.TransactionResponse;
import com.exptrack.expense.security.UserPrincipal;
import com.exptrack.expense.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<TransactionResponse> createTransaction(@AuthenticationPrincipal UserPrincipal user,
                                                                 @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(user.userId(), request));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> listTransactions(@AuthenticationPrincipal UserPrincipal user,
                                                                      @ModelAttribute TransactionFilter filter,
                                                                      Pageable pageable) {
        return ResponseEntity.ok(transactionService.list(user.userId(), filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@AuthenticationPrincipal UserPrincipal user,
                                                              @PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.find(user.userId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@AuthenticationPrincipal UserPrincipal user,
                                                                 @PathVariable UUID id,
                                                                 @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.update(user.userId(), id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@AuthenticationPrincipal UserPrincipal user,
                                  @PathVariable UUID id) {
        transactionService.delete(user.userId(), id);
    }
}
