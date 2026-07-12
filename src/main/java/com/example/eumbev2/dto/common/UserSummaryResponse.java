package com.example.eumbev2.dto.common;

import com.example.eumbev2.entity.user.User;

public record UserSummaryResponse(
        Long userId,
        String username,
        String nickname,
        String avatarUrl
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getUsername(), user.getNickname(), user.getAvatarUrl());
    }
}
