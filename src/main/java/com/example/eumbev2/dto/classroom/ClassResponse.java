package com.example.eumbev2.dto.classroom;

import com.example.eumbev2.dto.common.UserSummaryResponse;
import com.example.eumbev2.entity.classroom.AccessScope;
import com.example.eumbev2.entity.classroom.ClassStatus;
import com.example.eumbev2.entity.classroom.Classroom;

import java.time.Instant;
import java.util.List;

public record ClassResponse(
        Long id,
        String title,
        String description,
        Integer difficulty,
        List<String> tags,
        AccessScope accessScope,
        ClassStatus status,
        String bannerImageUrl,
        UserSummaryResponse mentor,
        long menteeCount,
        Instant createdAt,
        Instant updatedAt,
        String mentorIntroduction,
        String guide
) {
    public static ClassResponse of(Classroom c, long menteeCount) {
        return new ClassResponse(
                c.getId(), c.getTitle(), c.getDescription(), c.getDifficulty(), c.getTags(),
                c.getAccessScope(), c.getStatus(), c.getBannerImageUrl(),
                UserSummaryResponse.from(c.getMentor()), menteeCount,
                c.getCreatedAt(), c.getUpdatedAt(), c.getMentorIntroduction(), c.getGuide()
        );
    }
}
