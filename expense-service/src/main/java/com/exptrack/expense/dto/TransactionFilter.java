package com.exptrack.expense.dto;

import com.exptrack.expense.domain.TransactionType;

import java.time.LocalDate;
import java.util.UUID;

public record TransactionFilter(
        TransactionType type,
        UUID categoryId,
        LocalDate fromDate,
        LocalDate toDate
) {}
