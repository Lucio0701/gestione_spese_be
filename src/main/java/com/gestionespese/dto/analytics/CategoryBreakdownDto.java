package com.gestionespese.dto.analytics;

import java.util.List;

public record CategoryBreakdownDto(
    List<CategoryTotal> categories
) {
    public record CategoryTotal(
        String categoryId,
        Double total,
        List<TagTotal> tags
    ) {
    }

    public record TagTotal(String tag, Double total) {
    }
}
