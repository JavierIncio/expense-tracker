package com.exptrack.expense.web;

import com.exptrack.expense.dto.*;
import com.exptrack.expense.security.UserPrincipal;
import com.exptrack.expense.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public BudgetResponse createBudget(@AuthenticationPrincipal UserPrincipal user,
                                       @Valid @RequestBody BudgetRequest request) {
        return budgetService.create(user.userId(), request);
    }

    @GetMapping
    public Page<BudgetResponse> listBudgets(@AuthenticationPrincipal UserPrincipal user,
                                            @ModelAttribute BudgetFilter filter,
                                            Pageable pageable) {
        return budgetService.list(user.userId(), filter, pageable);
    }

    @GetMapping("/{id}")
    public BudgetResponse getBudget(@AuthenticationPrincipal UserPrincipal user,
                                    @PathVariable UUID id) {
        return budgetService.find(user.userId(), id);
    }

    @PutMapping("/{id}")
    public BudgetResponse updateBudget(@AuthenticationPrincipal UserPrincipal user,
                                       @PathVariable UUID id,
                                       @Valid @RequestBody BudgetRequest request) {
        return budgetService.update(user.userId(), id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteBudget(@AuthenticationPrincipal UserPrincipal user,
                             @PathVariable UUID id) {
        budgetService.delete(user.userId(), id);
    }
}
