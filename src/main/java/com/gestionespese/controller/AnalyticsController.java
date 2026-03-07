package com.gestionespese.controller;

import com.gestionespese.dto.analytics.AnalyticsSummaryDto;
import com.gestionespese.dto.analytics.CategoryBreakdownDto;
import com.gestionespese.dto.analytics.MonthlyReportDto;
import com.gestionespese.dto.analytics.SpendingPredictionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryDto> summary(@RequestParam(required = false) String dateFrom,
                                                       @RequestParam(required = false) String dateTo,
                                                       @RequestParam(required = false) String groupBy) {
        return ResponseEntity.ok(new AnalyticsSummaryDto(0.0, 0.0, 0.0, java.util.List.of()));
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<CategoryBreakdownDto> breakdown(@RequestParam(required = false) String dateFrom,
                                                          @RequestParam(required = false) String dateTo) {
        return ResponseEntity.ok(new CategoryBreakdownDto(java.util.List.of()));
    }

    @GetMapping("/predict-spending")
    public ResponseEntity<SpendingPredictionDto> predict(@RequestParam String period) {
        return ResponseEntity.ok(new SpendingPredictionDto(period, 0.0, 0.0, java.util.List.of()));
    }

    @GetMapping("/monthly-report")
    public ResponseEntity<MonthlyReportDto> report(@RequestParam String month) {
        return ResponseEntity.ok(new MonthlyReportDto(month, java.util.List.of(), new AnalyticsSummaryDto(0.0, 0.0, 0.0, java.util.List.of()), new CategoryBreakdownDto(java.util.List.of())));
    }
}
