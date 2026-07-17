package com.example.eumbev2.controller;

import com.example.eumbev2.common.exception.ApiException;
import com.example.eumbev2.common.exception.ErrorCode;
import com.example.eumbev2.dto.auth.*;
import com.example.eumbev2.service.auth.AuthRateLimitService;
import com.example.eumbev2.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthRateLimitService authRateLimitService;

    public AuthController(AuthService authService, AuthRateLimitService authRateLimitService) {
        this.authService = authService;
        this.authRateLimitService = authRateLimitService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthTokensResponse signup(@Valid @RequestBody SignupRequest request, HttpServletRequest servletRequest) {
        return authService.signup(request, servletRequest.getHeader("User-Agent"));
    }

    @PostMapping("/signin")
    public AuthTokensResponse signin(@Valid @RequestBody SigninRequest request, HttpServletRequest servletRequest) {
        authRateLimitService.checkSignin(servletRequest, request.username());
        try {
            AuthTokensResponse response = authService.signin(request, servletRequest.getHeader("User-Agent"));
            authRateLimitService.clearSigninFailures(request.username());
            return response;
        } catch (ApiException ex) {
            if (ex.getErrorCode() == ErrorCode.INVALID_CREDENTIALS) {
                authRateLimitService.recordSigninFailure(request.username());
            }
            throw ex;
        }
    }

    @PostMapping("/refresh")
    public AuthTokensResponse refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest servletRequest) {
        authRateLimitService.checkRefresh(servletRequest, request.refreshToken());
        return authService.refresh(request);
    }

    @PostMapping("/signout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signout(@Valid @RequestBody RefreshRequest request) {
        authService.signout(request);
    }

    @PostMapping("/signout-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signoutAll() {
        authService.signoutAll();
    }

    @GetMapping("/check-username")
    public AvailabilityResponse checkUsername(@RequestParam String username) {
        return authService.checkUsernameAvailable(username);
    }

    @GetMapping("/check-email")
    public AvailabilityResponse checkEmail(@RequestParam String email) {
        return authService.checkEmailAvailable(email);
    }

    @PostMapping("/email/send")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendEmailVerification(@Valid @RequestBody EmailSendRequest request, HttpServletRequest servletRequest) {
        authRateLimitService.checkEmailSend(servletRequest, request.email());
        authService.sendEmailVerificationCode(request);
    }

    @PostMapping("/email/verify")
    public VerificationResultResponse verifyEmail(@Valid @RequestBody EmailVerifyRequest request, HttpServletRequest servletRequest) {
        authRateLimitService.checkEmailVerify(servletRequest, request.email());
        try {
            VerificationResultResponse response = authService.verifyEmailCode(request);
            authRateLimitService.clearEmailVerifyFailures(request.email());
            return response;
        } catch (ApiException ex) {
            if (ex.getErrorCode() == ErrorCode.INVALID_VERIFICATION_CODE) {
                authRateLimitService.recordEmailVerifyFailure(request.email());
            }
            throw ex;
        }
    }

    @PostMapping("/password/reset-request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestPasswordReset(@Valid @RequestBody PasswordResetRequest request, HttpServletRequest servletRequest) {
        authRateLimitService.checkPasswordResetRequest(servletRequest, request.email());
        authService.requestPasswordReset(request);
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request, HttpServletRequest servletRequest) {
        authRateLimitService.checkPasswordReset(servletRequest, request.token());
        authService.confirmPasswordReset(request);
    }
}
