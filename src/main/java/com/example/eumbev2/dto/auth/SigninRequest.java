package com.example.eumbev2.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record SigninRequest(
        @NotBlank String username,
        @NotBlank String password,
        Boolean keepSignedIn
) {
    public boolean keepSignedInOrDefault() {
        return keepSignedIn != null && keepSignedIn;
    }
}
