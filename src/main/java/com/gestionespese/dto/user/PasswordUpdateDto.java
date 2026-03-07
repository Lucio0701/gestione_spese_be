package com.gestionespese.dto.user;

public record PasswordUpdateDto(
        String currentPassword,
        String newPassword) {
}
