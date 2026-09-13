package com.exptrack.expense.web;

import com.exptrack.expense.dto.*;
import com.exptrack.expense.security.UserPrincipal;
import com.exptrack.expense.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "Crea un presupuesto",
            description = "Crea un límite mensual para una categoría del usuario. No puede existir otro presupuesto para la misma categoría y mes.",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Presupuesto creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada para el usuario"),
            @ApiResponse(responseCode = "409", description = "Ya existe un presupuesto para esa categoría y mes")
    })
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@AuthenticationPrincipal UserPrincipal user,
                                                       @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.create(user.userId(), request));
    }

    @Operation(summary = "Lista presupuestos",
            description = "Devuelve presupuestos del usuario paginados y filtrables por categoría y mes.",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponse(responseCode = "200", description = "Página de presupuestos")
    @GetMapping
    public ResponseEntity<Page<BudgetResponse>> listBudgets(@AuthenticationPrincipal UserPrincipal user,
                                                            @ModelAttribute BudgetFilter filter,
                                                            Pageable pageable) {
        return ResponseEntity.ok(budgetService.list(user.userId(), filter, pageable));
    }

    @Operation(summary = "Obtiene un presupuesto",
            description = "Devuelve el presupuesto siempre que pertenezca al usuario autenticado.",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presupuesto encontrado"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado para el usuario")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudget(@AuthenticationPrincipal UserPrincipal user,
                                                    @PathVariable UUID id) {
        return ResponseEntity.ok(budgetService.find(user.userId(), id));
    }

    @Operation(summary = "Actualiza un presupuesto",
            description = "Modifica el límite mensual de un presupuesto existente del usuario.",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presupuesto actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Presupuesto o categoría no encontrados para el usuario"),
            @ApiResponse(responseCode = "409", description = "Ya existe un presupuesto para esa categoría y mes")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(@AuthenticationPrincipal UserPrincipal user,
                                                       @PathVariable UUID id,
                                                       @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.update(user.userId(), id, request));
    }

    @Operation(summary = "Elimina un presupuesto",
            description = "Elimina el presupuesto del usuario autenticado.",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Presupuesto eliminado"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado para el usuario")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBudget(@AuthenticationPrincipal UserPrincipal user,
                             @PathVariable UUID id) {
        budgetService.delete(user.userId(), id);
    }
}
