package com.exptrack.expense.web;

import com.exptrack.expense.dto.MonthlySummaryResponse;
import com.exptrack.expense.security.UserPrincipal;
import com.exptrack.expense.service.SummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @Operation(summary = "Resumen mensual",
            description = "Devuelve total de ingresos, gastos, balance y el desglose por categoría con el estado del presupuesto (WITHIN_LIMIT, EXCEEDED o NO_BUDGET) para el mes indicado.",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen mensual"),
            @ApiResponse(responseCode = "400", description = "Parámetros obligatorios ausentes o inválidos")
    })
    @GetMapping("/monthly")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(@AuthenticationPrincipal UserPrincipal user,
                                                                    @RequestParam int year,
                                                                    @RequestParam int month) {
        return ResponseEntity.ok(summaryService.monthlySummary(user.userId(), year, month));
    }
}
