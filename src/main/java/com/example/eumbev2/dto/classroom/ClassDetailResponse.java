package com.example.eumbev2.dto.classroom;

import com.example.eumbev2.dto.common.UserSummaryResponse;
import com.example.eumbev2.entity.classroom.AccessScope;
import com.example.eumbev2.entity.classroom.ClassStatus;
import com.example.eumbev2.entity.classroom.Classroom;
import com.example.eumbev2.entity.classroom.Role;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record ClassDetailResponse(
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
        String inviteCode,
        long assignmentCount,
        Role myRole,
        Instant createdAt,
        Instant updatedAt,
        String mentorIntroduction,
        String guide
) {
    public static ClassDetailResponse of(Classroom c, long menteeCount, String inviteCode, long assignmentCount, Role myRole) {
        return new ClassDetailResponse(
                c.getId(), c.getTitle(), c.getDescription(), c.getDifficulty(),
                c.getTags() == null ? List.of() : new ArrayList<>(c.getTags()),
                c.getAccessScope(), c.getStatus(), c.getBannerImageUrl(),
                UserSummaryResponse.from(c.getMentor()), menteeCount, inviteCode, assignmentCount, myRole,
                c.getCreatedAt(), c.getUpdatedAt(), c.getMentorIntroduction(), c.getGuide()
        );
    }
}
