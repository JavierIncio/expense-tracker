package com.exptrack.expense.web;

import com.exptrack.expense.dto.MonthlySummaryResponse;
import com.exptrack.expense.security.UserPrincipal;
import com.exptrack.expense.service.SummaryService;
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

    @GetMapping("/monthly")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(@AuthenticationPrincipal UserPrincipal user,
                                                                    @RequestParam int year,
                                                                    @RequestParam int month) {
        return ResponseEntity.ok(summaryService.monthlySummary(user.userId(), year, month));
    }
}
