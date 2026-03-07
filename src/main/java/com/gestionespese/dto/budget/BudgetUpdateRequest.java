package com.gestionespese.dto.budget;

import java.time.LocalDate;
import java.util.List;

public record BudgetUpdateRequest(
    String categoryId,
    Double amount,
    BudgetPeriod period,
    LocalDate startDate,
    LocalDate endDate,
    List<String> tags
) {
}
