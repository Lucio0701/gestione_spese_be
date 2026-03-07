package com.gestionespese.dto.expense;

public record ImportJobDto(
    String jobId,
    Status status
) {
    public enum Status {QUEUED, PROCESSING, COMPLETED, FAILED}
}
