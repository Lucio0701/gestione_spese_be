package com.gestionespese.dto.goal;

import java.time.LocalDate;

public record GoalUpdateRequest(
    String title,
    Double targetAmount,
    Double currentAmount,
    LocalDate deadline
) {
}
