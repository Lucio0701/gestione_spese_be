package com.gestionespese.dto.expense;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record ExpenseDto(
                String id,
                String userId,
                String categoryId,
                Double amount,
                String currency,
                LocalDate date,
                String description,
                com.gestionespese.model.TransactionType type,
                List<String> tags,
                com.gestionespese.model.Recurrence recurrence,
                OffsetDateTime createdAt,
                OffsetDateTime updatedAt) {
}
