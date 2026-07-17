package com.example.eumbev2.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Application-level error codes. The enum name is used as the "code" field in
 * {@link com.example.eumbev2.common.response.ErrorResponse}, matching the API spec's Error schema.
 */
public enum ErrorCode {

    // Generic
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // Auth
    USERNAME_TAKEN(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    EMAIL_TAKEN(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다."),
    PRIVACY_NOT_AGREED(HttpStatus.BAD_REQUEST, "개인정보 수집·이용에 동의해야 합니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증코드가 올바르지 않거나 만료되었습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "액세스 토큰이 유효하지 않습니다."),
    INVALID_RESET_TOKEN(HttpStatus.BAD_REQUEST, "재설정 토큰이 유효하지 않거나 만료되었습니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),

    // Classroom
    CLASS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 클래스입니다."),
    NOT_CLASS_MENTOR(HttpStatus.FORBIDDEN, "클래스 멘토만 수행할 수 있습니다."),

    // Member / Invite / Join
    ALREADY_MEMBER(HttpStatus.CONFLICT, "이미 가입했거나 신청한 클래스입니다."),
    INVITE_CODE_REQUIRED(HttpStatus.BAD_REQUEST, "초대 코드가 필요합니다."),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "초대 코드가 올바르지 않거나 만료되었습니다."),
    NO_INVITE_CODE(HttpStatus.NOT_FOUND, "발급된 초대 코드가 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 멤버입니다."),
    NOT_WAITING_MEMBER(HttpStatus.CONFLICT, "승인 대기 중인 신청이 아닙니다."),
    NOT_CLASS_MEMBER(HttpStatus.FORBIDDEN, "클래스 멤버만 접근할 수 있습니다."),

    // Application form
    APPLICATION_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 지원서 질문입니다."),

    // Assignment
    ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 과제입니다."),

    // Submission
    SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 제출물입니다."),
    ALREADY_SUBMITTED(HttpStatus.CONFLICT, "이미 제출한 과제입니다."),
    NOT_CLASS_MENTEE(HttpStatus.FORBIDDEN, "클래스 멘티만 수행할 수 있습니다."),
    NOT_SUBMISSION_OWNER(HttpStatus.FORBIDDEN, "본인의 제출물만 접근할 수 있습니다."),

    // Notice
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 공지사항입니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
