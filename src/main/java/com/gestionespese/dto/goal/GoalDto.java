package com.gestionespese.dto.goal;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record GoalDto(
    String id,
    String userId,
    String title,
    Double targetAmount,
    Double currentAmount,
    LocalDate deadline,
    OffsetDateTime createdAt
) {
}
