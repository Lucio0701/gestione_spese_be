package com.gestionespese.dto.budget;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record BudgetDto(
        String id,
        String userId,
        String categoryId,
        Double amount,
        Double spent,
        BudgetPeriod period,
        LocalDate startDate,
        LocalDate endDate,
        List<String> tags,
        OffsetDateTime createdAt) {
}
