package com.example.eumbev2.dto.member;

import com.example.eumbev2.dto.application.ApplicationAnswerDto;
import com.example.eumbev2.dto.common.UserSummaryResponse;
import com.example.eumbev2.entity.classroom.ClassroomMember;

import java.time.Instant;
import java.util.List;

public record WaitingMemberResponse(UserSummaryResponse user, String message, Instant appliedAt, List<ApplicationAnswerDto> answers) {
    public static WaitingMemberResponse of(ClassroomMember member, List<ApplicationAnswerDto> answers) {
        return new WaitingMemberResponse(
                UserSummaryResponse.from(member.getUser()),
                member.getMessage(),
                member.getAppliedAt(),
                answers
        );
    }
}
