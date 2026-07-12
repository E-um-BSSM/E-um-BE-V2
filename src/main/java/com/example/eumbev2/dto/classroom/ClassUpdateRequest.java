package com.example.eumbev2.dto.classroom;

import com.example.eumbev2.entity.classroom.AccessScope;
import com.example.eumbev2.entity.classroom.ClassStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

/** Partial update: only non-null fields are applied. */
public record ClassUpdateRequest(
        String title,
        String description,
        @Min(1) @Max(5) Integer difficulty,
        List<String> tags,
        AccessScope accessScope,
        ClassStatus status,
        String bannerImageUrl,
        String mentorIntroduction,
        String guide
) {
}
