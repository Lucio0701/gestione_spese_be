package com.gestionespese.repository;

import com.gestionespese.dto.budget.BudgetPeriod;
import com.gestionespese.model.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, String> {
    Page<Budget> findByUserIdAndPeriodAndCategoryId(Long userId, BudgetPeriod period, String categoryId,
            Pageable pageable);

    // Fallback queries for optional parameters can be handled via Example or
    // Specification,
    // but for simplicity we can just list common permutations or use a Query if
    // needed.
    // For now simple finding by user is enough as starting point
    Page<Budget> findByUserId(Long userId, Pageable pageable);
}
