package com.gestionespese.dto.expense;

import java.util.List;

public record PagedExpenses(
    List<ExpenseDto> content,
    Integer page,
    Integer size,
    Long totalElements,
    Integer totalPages
) {
}
