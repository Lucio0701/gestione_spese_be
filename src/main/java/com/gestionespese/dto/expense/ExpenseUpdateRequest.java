package com.gestionespese.dto.expense;

public record ExpenseUpdateRequest(
                Double amount,
                String currency,
                String categoryId,
                String description,
                java.time.LocalDate date,
                com.gestionespese.model.TransactionType type,
                java.util.List<String> tags,
                com.gestionespese.model.Recurrence recurrence) {
}
