package com.gestionespese.dto.auth;

public record ResetPasswordRequest(String token, String newPassword) {}
