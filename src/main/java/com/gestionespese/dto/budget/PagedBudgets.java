package com.gestionespese.dto.budget;

import java.util.List;

public record PagedBudgets(
    List<BudgetDto> content,
    Integer page,
    Integer size,
    Long totalElements,
    Integer totalPages
) {
}
