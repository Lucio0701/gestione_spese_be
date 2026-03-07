package com.gestionespese.dto.budget;

public record BudgetStatusDto(
    String budgetId,
    Double spent,
    Double remaining,
    Double percentage,
    java.time.LocalDate projectedEnd
) {
}
