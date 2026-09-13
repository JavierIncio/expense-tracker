package com.exptrack.expense.web;

import com.exptrack.expense.dto.TransactionFilter;
import com.exptrack.expense.dto.TransactionRequest;
import com.exptrack.expense.dto.TransactionResponse;
import com.exptrack.expense.security.UserPrincipal;
import com.exptrack.expense.service.TransactionService;
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
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Crea una transacción",
            description = "Persiste un ingreso o gasto verificando que la categoría pertenezca al usuario y que el tipo coincida. Si un gasto supera el presupuesto del mes, publica un evento de presupuesto excedido.",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transacción creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o tipo de transacción no coincide con la categoría"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada para el usuario")
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@AuthenticationPrincipal UserPrincipal user,
                                                                 @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(user.userId(), request));
    }

    @Operation(summary = "Lista transacciones",
            description = "Devuelve transacciones del usuario paginadas y filtrables por tipo, categoría y rango de fechas. Ordenable por cualquier campo; por defecto ordena por fecha descendente.",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponse(responseCode = "200", description = "Página de transacciones")
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> listTransactions(@AuthenticationPrincipal UserPrincipal user,
                                                                      @ModelAttribute TransactionFilter filter,
                                                                      Pageable pageable) {
        return ResponseEntity.ok(transactionService.list(user.userId(), filter, pageable));
    }

    @Operation(summary = "Obtiene una transacción",
            description = "Devuelve la transacción siempre que pertenezca al usuario autenticado.",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transacción encontrada"),
            @ApiResponse(responseCode = "404", description = "Transacción no encontrada para el usuario")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@AuthenticationPrincipal UserPrincipal user,
                                                              @PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.find(user.userId(), id));
    }

    @Operation(summary = "Actualiza una transacción",
            description = "Modifica una transacción existente del usuario verificando categoría y tipo. Si un gasto supera el presupuesto del mes, publica un evento de presupuesto excedido.",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transacción actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o tipo de transacción no coincide con la categoría"),
            @ApiResponse(responseCode = "404", description = "Transacción o categoría no encontradas para el usuario")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@AuthenticationPrincipal UserPrincipal user,
                                                                 @PathVariable UUID id,
                                                                 @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.update(user.userId(), id, request));
    }

    @Operation(summary = "Elimina una transacción",
            description = "Elimina la transacción del usuario autenticado.",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Transacción eliminada"),
            @ApiResponse(responseCode = "404", description = "Transacción no encontrada para el usuario")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@AuthenticationPrincipal UserPrincipal user,
                                  @PathVariable UUID id) {
        transactionService.delete(user.userId(), id);
    }
}
