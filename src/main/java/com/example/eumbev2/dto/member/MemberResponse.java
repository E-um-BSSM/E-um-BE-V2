package com.example.eumbev2.dto.member;

import com.example.eumbev2.dto.common.UserSummaryResponse;
import com.example.eumbev2.entity.classroom.ClassroomMember;
import com.example.eumbev2.entity.classroom.MemberStatus;
import com.example.eumbev2.entity.classroom.Role;

import java.time.Instant;

public record MemberResponse(UserSummaryResponse user, Role role, MemberStatus status, Instant joinedAt) {
    public static MemberResponse from(ClassroomMember member) {
        return new MemberResponse(
                UserSummaryResponse.from(member.getUser()),
                member.getRole(),
                member.getStatus(),
                member.getJoinedAt()
        );
    }
}
