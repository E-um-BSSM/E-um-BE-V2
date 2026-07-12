package com.example.eumbev2.dto.assignment;

import com.example.eumbev2.entity.assignment.Assignment;
import com.example.eumbev2.entity.submission.SubmissionStatus;

import java.time.Instant;

public record AssignmentResponse(
        Long id,
        Long classId,
        String title,
        String description,
        Instant dueDate,
        Integer difficulty,
        long submissionCount,
        SubmissionStatus mySubmissionStatus,
        Instant createdAt
) {
    public static AssignmentResponse of(Assignment a, long submissionCount, SubmissionStatus mySubmissionStatus) {
        return new AssignmentResponse(
                a.getId(), a.getClassroom().getId(), a.getTitle(), a.getDescription(),
                a.getDueDate(), a.getDifficulty(), submissionCount, mySubmissionStatus, a.getCreatedAt()
        );
    }
}
