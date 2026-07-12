package com.example.eumbev2.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(min = 4, max = 20) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotNull Boolean privacyAgreed
) {
}
