package com.example.eumbev2.dto.classroom;

import com.example.eumbev2.dto.common.UserSummaryResponse;
import com.example.eumbev2.entity.classroom.ClassStatus;
import com.example.eumbev2.entity.classroom.Classroom;

public record ClassSummaryResponse(
        Long id,
        String title,
        String bannerImageUrl,
        Integer difficulty,
        ClassStatus status,
        UserSummaryResponse mentor,
        long menteeCount
) {
    public static ClassSummaryResponse of(Classroom c, long menteeCount) {
        return new ClassSummaryResponse(
                c.getId(), c.getTitle(), c.getBannerImageUrl(), c.getDifficulty(), c.getStatus(),
                UserSummaryResponse.from(c.getMentor()), menteeCount
        );
    }
}
