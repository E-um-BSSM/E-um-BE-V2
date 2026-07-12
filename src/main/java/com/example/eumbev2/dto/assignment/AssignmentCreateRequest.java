package com.example.eumbev2.dto.assignment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record AssignmentCreateRequest(
        @NotBlank String title,
        String description,
        Instant dueDate,
        @Min(1) @Max(5) Integer difficulty
) {
}
