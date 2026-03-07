package com.gestionespese.dto.user;

import java.time.OffsetDateTime;

public record UserProfile(
        String id,
        String email,
        String firstName,
        String lastName,
        OffsetDateTime createdAt) {
}
