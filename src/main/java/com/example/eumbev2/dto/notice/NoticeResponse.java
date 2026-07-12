package com.example.eumbev2.dto.notice;

import com.example.eumbev2.dto.common.UserSummaryResponse;
import com.example.eumbev2.entity.notice.Notice;

import java.time.Instant;

public record NoticeResponse(
        Long id,
        Long classId,
        String title,
        String content,
        UserSummaryResponse author,
        Instant createdAt,
        Instant updatedAt
) {
    public static NoticeResponse from(Notice n) {
        return new NoticeResponse(
                n.getId(), n.getClassroom().getId(), n.getTitle(), n.getContent(),
                UserSummaryResponse.from(n.getAuthor()), n.getCreatedAt(), n.getUpdatedAt()
        );
    }
}
