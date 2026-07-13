package com.example.eumbev2.dto.member;

import com.example.eumbev2.dto.application.ApplicationAnswerDto;

import java.util.List;

/**
 * 초대 코드만으로 가입 신청하는 요청. classId를 모르는 진입점(내 클래스의 코드 입력 모달 등)에서 사용한다.
 */
public record JoinByCodeRequest(String inviteCode, String message, List<ApplicationAnswerDto> answers) {
}
