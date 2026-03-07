package com.gestionespese.dto.analytics;

import java.util.List;

public record AnalyticsSummaryDto(
    Double totalSpent,
    Double totalBudget,
    Double totalRemaining,
    List<TimelinePoint> timeline
) {
    public record TimelinePoint(String period, Double amount) {
    }
}
