package com.gestionespese.repository;

import com.gestionespese.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, String> {

        @Query(value = "SELECT * FROM expenses e WHERE e.user_id = :userId " +
                        "AND (:categoryId IS NULL OR e.category_id = :categoryId) " +
                        "AND (CAST(:dateFrom AS date) IS NULL OR e.date >= :dateFrom) " +
                        "AND (CAST(:dateTo AS date) IS NULL OR e.date <= :dateTo) " +
                        "AND (:minAmount IS NULL OR e.amount >= :minAmount) " +
                        "AND (:maxAmount IS NULL OR e.amount <= :maxAmount) " +
                        "AND (:search IS NULL OR LOWER(convert_from(CAST(e.description AS bytea), 'UTF8')) LIKE LOWER(CONCAT('%', :search, '%')))", nativeQuery = true)
        Page<Expense> findExpenses(
                        @Param("userId") Long userId,
                        @Param("categoryId") String categoryId,
                        @Param("dateFrom") LocalDate dateFrom,
                        @Param("dateTo") LocalDate dateTo,
                        @Param("minAmount") Double minAmount,
                        @Param("maxAmount") Double maxAmount,
                        @Param("search") String search,
                        Pageable pageable);

        @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.categoryId = :categoryId AND e.date >= :startDate AND e.date <= :endDate")
        Double getSumAmount(@Param("userId") String userId, @Param("categoryId") String categoryId,
                        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
