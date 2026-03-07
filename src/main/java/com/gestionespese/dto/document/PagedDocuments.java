package com.gestionespese.dto.document;

import java.util.List;

public record PagedDocuments(
    List<DocumentDto> content,
    Integer page,
    Integer size,
    Long totalElements,
    Integer totalPages
) {
}
