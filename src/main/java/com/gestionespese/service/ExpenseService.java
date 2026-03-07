package com.gestionespese.service;

import com.gestionespese.dto.expense.ExpenseCreateRequest;
import com.gestionespese.dto.expense.ExpenseDto;
import com.gestionespese.dto.expense.ExpenseUpdateRequest;
import com.gestionespese.dto.expense.PagedExpenses;
import com.gestionespese.model.Expense;
import com.gestionespese.model.User;
import com.gestionespese.repository.ExpenseRepository;
import com.gestionespese.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public PagedExpenses getExpenses(String email, int page, int size, String sort, LocalDate dateFrom,
            LocalDate dateTo, String categoryId, Double minAmount, Double maxAmount, String search) {
        User user = userRepository.findByEmail(email).orElseThrow();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date")); // TODO: parse sort
                                                                                                    // string

        Page<Expense> expensePage = expenseRepository.findExpenses(
                user.getId(), categoryId, dateFrom, dateTo, minAmount, maxAmount, search, pageRequest);

        List<ExpenseDto> dtos = expensePage.getContent().stream().map(this::mapToDto).collect(Collectors.toList());
        return new PagedExpenses(dtos, page, size, expensePage.getTotalElements(), expensePage.getTotalPages());
    }

    public ExpenseDto createExpense(String email, ExpenseCreateRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Expense expense = new Expense(
                user,
                request.amount(),
                request.date(),
                request.description(),
                request.categoryId(),
                request.type(),
                request.tags(),
                request.recurrence());
        Expense saved = expenseRepository.save(expense);
        return mapToDto(saved);
    }

    public ExpenseDto updateExpense(String id, ExpenseUpdateRequest request) {
        Expense expense = expenseRepository.findById(id).orElseThrow();
        // TODO: check ownership
        expense.setAmount(request.amount());
        expense.setDate(request.date());
        expense.setDescription(request.description());
        expense.setCategoryId(request.categoryId());
        expense.setType(request.type());
        expense.setTags(request.tags());
        expense.setRecurrence(request.recurrence());
        return mapToDto(expenseRepository.save(expense));
    }

    public void deleteExpense(String id, String email) {
        Expense expense = expenseRepository.findById(id).orElseThrow(() -> new RuntimeException("Expense not found"));
        if (!expense.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not authorized to delete this expense");
        }
        expenseRepository.delete(expense);
    }

    public ExpenseDto getExpense(String id) {
        return mapToDto(expenseRepository.findById(id).orElseThrow());
    }

    private ExpenseDto mapToDto(Expense e) {
        return new ExpenseDto(
                e.getId(),
                e.getUser().getId().toString(),
                e.getCategoryId(),
                e.getAmount(),
                e.getCurrency(),
                e.getDate(),
                e.getDescription(),
                e.getType(),
                e.getTags(),
                e.getRecurrence(), // Recurrence
                null, // CreatedAt
                null // UpdatedAt
        );
    }

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 0 * * *") // Every day at midnight
    @org.springframework.transaction.annotation.Transactional
    public void processRecurringExpenses() {
        LocalDate today = LocalDate.now();
        List<Expense> expenses = expenseRepository.findAll(); // Optimization: find only recurring not processed
                                                              // expenses

        for (Expense expense : expenses) {
            if (expense.getRecurrence() != null && expense.getRecurrence() != com.gestionespese.model.Recurrence.NONE
                    && !Boolean.TRUE.equals(expense.getIsRecurrenceProcessed())) {
                LocalDate nextDate = null;
                switch (expense.getRecurrence()) {
                    case DAILY:
                        nextDate = expense.getDate().plusDays(1);
                        break;
                    case WEEKLY:
                        nextDate = expense.getDate().plusWeeks(1);
                        break;
                    case MONTHLY:
                        nextDate = expense.getDate().plusMonths(1);
                        break;
                    case YEARLY:
                        nextDate = expense.getDate().plusYears(1);
                        break;
                    default:
                        break;
                }

                if (nextDate != null && !nextDate.isAfter(today)) {
                    // Create new expense
                    Expense newExpense = new Expense(
                            expense.getUser(),
                            expense.getAmount(),
                            nextDate,
                            expense.getDescription(),
                            expense.getCategoryId(),
                            expense.getType(),
                            new java.util.ArrayList<>(expense.getTags()),
                            expense.getRecurrence());
                    expenseRepository.save(newExpense);

                    // Mark old as processed to avoid double processing (simple logic, can be
                    // improved)
                    expense.setIsRecurrenceProcessed(true);
                    expenseRepository.save(expense);
                }
            }
        }
    }
}
