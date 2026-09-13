package com.exptrack.expense.dto;

import com.exptrack.expense.domain.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public interface CategoryTotal {
    UUID getCategoryId();
    TransactionType getType();
    BigDecimal getTotal();
}
