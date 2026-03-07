package com.gestionespese.dto.budget;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record BudgetCreateRequest(
    String categoryId,
    @NotNull Double amount,
    @NotNull BudgetPeriod period,
    LocalDate startDate,
    LocalDate endDate,
    List<String> tags
) {
}
