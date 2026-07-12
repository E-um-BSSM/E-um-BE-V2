package com.example.eumbev2.dto.submission;

import com.example.eumbev2.dto.common.UserSummaryResponse;
import com.example.eumbev2.entity.submission.Submission;
import com.example.eumbev2.entity.submission.SubmissionStatus;

import java.time.Instant;

public record SubmissionResponse(
        Long id,
        Long assignmentId,
        UserSummaryResponse mentee,
        String content,
        String fileUrl,
        SubmissionStatus status,
        Integer score,
        String feedback,
        Instant submittedAt,
        Instant gradedAt
) {
    public static SubmissionResponse from(Submission s) {
        return new SubmissionResponse(
                s.getId(), s.getAssignment().getId(), UserSummaryResponse.from(s.getUser()),
                s.getContent(), s.getFileUrl(), s.getStatus(), s.getScore(), s.getFeedback(),
                s.getSubmittedAt(), s.getGradedAt()
        );
    }
}
