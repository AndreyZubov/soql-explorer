package io.soqlexplorer.web.connections;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Inbound payload for {@code POST /connections/oauth/start}. */
public record StartOAuthRequest(
    @NotBlank @Pattern(regexp = "PRODUCTION|SANDBOX") String environment) {}
