package com.gestionespese.dto.document;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record DocumentDto(
    String id,
    String userId,
    String fileUrl,
    String type,
    String extractedText,
    LocalDate dueDate,
    OffsetDateTime createdAt
) {
}
