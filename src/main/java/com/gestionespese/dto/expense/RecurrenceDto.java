package com.gestionespese.dto.expense;

import java.time.LocalDate;

public record RecurrenceDto(
    Frequency frequency,
    Integer interval,
    LocalDate endDate
) {
    public enum Frequency {WEEKLY, MONTHLY, YEARLY}
}
