package com.example.eumbev2.dto.member;

import com.example.eumbev2.dto.application.ApplicationAnswerDto;

import java.util.List;

public record JoinRequest(String inviteCode, String message, List<ApplicationAnswerDto> answers) {
}
