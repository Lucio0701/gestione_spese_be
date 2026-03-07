package com.gestionespese.service;

import com.gestionespese.dto.budget.*;
import com.gestionespese.model.Budget;
import com.gestionespese.model.User;
import com.gestionespese.repository.BudgetRepository;
import com.gestionespese.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final com.gestionespese.repository.ExpenseRepository expenseRepository;

    public BudgetService(BudgetRepository budgetRepository, UserRepository userRepository,
            com.gestionespese.repository.ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
    }

    public PagedBudgets getBudgets(String email, int page, int size, BudgetPeriod period, String categoryId,
            LocalDate referenceDate) {
        User user = userRepository.findByEmail(email).orElseThrow();
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Budget> budgetPage = budgetRepository.findByUserId(user.getId(), pageRequest);

        LocalDate finalReferenceDate = referenceDate != null ? referenceDate : LocalDate.now();

        List<BudgetDto> dtos = budgetPage.getContent().stream()
                .map(b -> mapToDto(b, finalReferenceDate))
                .collect(Collectors.toList());
        return new PagedBudgets(dtos, page, size, budgetPage.getTotalElements(), budgetPage.getTotalPages());
    }

    // ... create, update, delete methods remain same ...

    public BudgetDto createBudget(String email, BudgetCreateRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Budget budget = new Budget(
                user,
                request.amount(),
                request.period(),
                request.categoryId());
        return mapToDto(budgetRepository.save(budget), LocalDate.now());
    }

    public BudgetDto updateBudget(String id, BudgetUpdateRequest request) {
        Budget budget = budgetRepository.findById(id).orElseThrow();
        if (request.amount() != null)
            budget.setAmount(request.amount());
        if (request.period() != null)
            budget.setPeriod(request.period());
        if (request.categoryId() != null)
            budget.setCategoryId(request.categoryId());
        return mapToDto(budgetRepository.save(budget), LocalDate.now());
    }

    public void deleteBudget(String id) {
        budgetRepository.deleteById(id);
    }

    public BudgetDto getBudget(String id) {
        return mapToDto(budgetRepository.findById(id).orElseThrow(), LocalDate.now());
    }

    private BudgetDto mapToDto(Budget b, LocalDate referenceDate) {
        LocalDate startDate = null;
        LocalDate endDate = null;

        // Calculate period based on BudgetPeriod
        switch (b.getPeriod()) {
            case WEEKLY:
                startDate = referenceDate
                        .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                endDate = referenceDate
                        .with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
                break;
            case MONTHLY:
                startDate = referenceDate.withDayOfMonth(1);
                endDate = referenceDate.withDayOfMonth(referenceDate.lengthOfMonth());
                break;
            case YEARLY:
                startDate = referenceDate.withDayOfYear(1);
                endDate = referenceDate.withDayOfYear(referenceDate.lengthOfYear());
                break;
            case CUSTOM:
                startDate = referenceDate.withDayOfMonth(1);
                endDate = referenceDate.withDayOfMonth(referenceDate.lengthOfMonth());
                break;
        }

        Double spent = 0.0;
        if (startDate != null && endDate != null && b.getCategoryId() != null) {
            spent = expenseRepository.getSumAmount(b.getUser().getId().toString(), b.getCategoryId(), startDate,
                    endDate);
            if (spent == null) {
                spent = 0.0;
            }
        }

        return new BudgetDto(
                b.getId(),
                b.getUser().getId().toString(),
                b.getCategoryId(),
                b.getAmount(),
                spent,
                b.getPeriod(),
                startDate,
                endDate,
                null, // Tags
                null // CreatedAt
        );
    }
}
