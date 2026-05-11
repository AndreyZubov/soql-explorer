package io.soqlexplorer.web.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Inbound payload for {@code POST /auth/login}. */
public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
