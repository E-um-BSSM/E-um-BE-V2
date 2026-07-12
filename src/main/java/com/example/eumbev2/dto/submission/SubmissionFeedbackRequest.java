package com.example.eumbev2.dto.submission;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmissionFeedbackRequest(
        @NotNull @Min(0) @Max(100) Integer score,
        @NotBlank String feedback
) {
}
