package com.example.eumbev2.dto.member;

import com.example.eumbev2.entity.classroom.Classroom;

import java.time.Instant;

public record InviteCodeResponse(Long classId, String code, Instant expiresAt) {
    public static InviteCodeResponse from(Classroom classroom) {
        return new InviteCodeResponse(classroom.getId(), classroom.getClassroomCode(), classroom.getClassroomCodeExpiresAt());
    }
}
