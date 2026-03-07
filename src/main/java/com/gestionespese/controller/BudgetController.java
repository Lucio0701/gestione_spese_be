package com.gestionespese.controller;

import com.gestionespese.dto.budget.*;
import com.gestionespese.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public ResponseEntity<PagedBudgets> getBudgets(
            Authentication authentication,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "categoryId", required = false) String categoryId,
            @RequestParam(name = "period", required = false) BudgetPeriod period,
            @RequestParam(name = "activeOn", required = false) LocalDate activeOn) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                budgetService.getBudgets(email, page, size, period, categoryId, activeOn));
    }

    @PostMapping
    public ResponseEntity<BudgetDto> createBudget(
            Authentication authentication,
            @Valid @RequestBody BudgetCreateRequest request) {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.createBudget(email, request));
    }

    @GetMapping("/{budgetId}")
    public ResponseEntity<BudgetDto> getBudget(@PathVariable("budgetId") String budgetId) {
        return ResponseEntity.ok(budgetService.getBudget(budgetId));
    }

    @PatchMapping("/{budgetId}")
    public ResponseEntity<BudgetDto> updateBudget(
            @PathVariable("budgetId") String budgetId,
            @Valid @RequestBody BudgetUpdateRequest request) {
        return ResponseEntity.ok(budgetService.updateBudget(budgetId, request));
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> deleteBudget(@PathVariable("budgetId") String budgetId) {
        budgetService.deleteBudget(budgetId);
        return ResponseEntity.noContent().build();
    }
}
