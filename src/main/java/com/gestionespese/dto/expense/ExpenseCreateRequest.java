package com.gestionespese.dto.expense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record ExpenseCreateRequest(
                @NotNull Double amount,
                String currency,
                @NotBlank String categoryId,
                @NotNull LocalDate date,
                String description,
                com.gestionespese.model.TransactionType type,
                List<String> tags,
                com.gestionespese.model.Recurrence recurrence) {
}
