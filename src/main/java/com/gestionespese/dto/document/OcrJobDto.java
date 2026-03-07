package com.gestionespese.dto.document;

public record OcrJobDto(
    String jobId,
    Status status
) {
    public enum Status {QUEUED, PROCESSING, COMPLETED, FAILED}
}
