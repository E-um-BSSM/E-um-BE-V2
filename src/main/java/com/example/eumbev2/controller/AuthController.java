package com.example.eumbev2.controller;

import com.example.eumbev2.dto.auth.*;
import com.example.eumbev2.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthTokensResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/signin")
    public AuthTokensResponse signin(@Valid @RequestBody SigninRequest request) {
        return authService.signin(request);
    }

    @PostMapping("/refresh")
    public AuthTokensResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/signout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signout() {
        authService.signout();
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
    public void sendEmailVerification(@Valid @RequestBody EmailSendRequest request) {
        authService.sendEmailVerificationCode(request);
    }

    @PostMapping("/email/verify")
    public VerificationResultResponse verifyEmail(@Valid @RequestBody EmailVerifyRequest request) {
        return authService.verifyEmailCode(request);
    }

    @PostMapping("/password/reset-request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
    }
}
