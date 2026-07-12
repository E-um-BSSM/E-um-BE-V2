package com.example.eumbev2.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailSendRequest(@NotBlank @Email String email) {
}
