package com.example.eumbev2.dto.classroom;

import com.example.eumbev2.entity.classroom.AccessScope;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ClassCreateRequest(
        @NotBlank String title,
        String description,
        @NotNull @Min(1) @Max(5) Integer difficulty,
        List<String> tags,
        @NotNull AccessScope accessScope,
        String bannerImageUrl,
        String mentorIntroduction,
        String guide
) {
}
