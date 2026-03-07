package com.gestionespese.dto.analytics;

import java.util.List;

public record MonthlyReportDto(
    String month,
    List<String> highlights,
    AnalyticsSummaryDto totals,
    CategoryBreakdownDto topCategories
) {
}
