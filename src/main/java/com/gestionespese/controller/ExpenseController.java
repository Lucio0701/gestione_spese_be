package com.gestionespese.controller;

import com.gestionespese.dto.expense.ExpenseCreateRequest;
import com.gestionespese.dto.expense.ExpenseDto;
import com.gestionespese.dto.expense.ExpenseUpdateRequest;
import com.gestionespese.dto.expense.PagedExpenses;
import com.gestionespese.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<PagedExpenses> getExpenses(
            Authentication authentication,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "dateFrom", required = false) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false) LocalDate dateTo,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "categoryId", required = false) String categoryId,
            @RequestParam(name = "minAmount", required = false) Double minAmount,
            @RequestParam(name = "maxAmount", required = false) Double maxAmount,
            @RequestParam(name = "search", required = false) String search) {
        String email = authentication.getName();

        if (month != null && year != null) {
            dateFrom = LocalDate.of(year, month, 1);
            dateTo = dateFrom.withDayOfMonth(dateFrom.lengthOfMonth());
        }

        return ResponseEntity.ok(
                expenseService.getExpenses(email, page, size, sort, dateFrom, dateTo, categoryId, minAmount, maxAmount,
                        search));
    }

    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(
            Authentication authentication,
            @Valid @RequestBody ExpenseCreateRequest request) {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpense(email, request));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseDto> getExpense(@PathVariable("expenseId") String expenseId) {
        return ResponseEntity.ok(expenseService.getExpense(expenseId));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseDto> updateExpense(
            @PathVariable("expenseId") String expenseId,
            @Valid @RequestBody ExpenseUpdateRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(expenseId, request));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(Authentication authentication,
            @PathVariable("expenseId") String expenseId) {
        String email = authentication.getName();
        expenseService.deleteExpense(expenseId, email);
        return ResponseEntity.noContent().build();
    }
}
