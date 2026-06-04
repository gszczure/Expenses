package org.example.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record SummaryResponseDto(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        Map<String, BigDecimal> expensesByCategory
) {
}
