package com.gestionespese.dto.analytics;

import java.util.List;

public record SpendingPredictionDto(
    String period,
    Double predictedAmount,
    Double confidence,
    List<String> drivers
) {
}
