package com.example.eumbev2.service.auth;

import com.example.eumbev2.common.exception.ApiException;
import com.example.eumbev2.common.exception.ErrorCode;
import com.example.eumbev2.common.security.JwtTokenProvider;
import com.example.eumbev2.common.security.SecurityUtils;
import com.example.eumbev2.common.util.CodeGenerator;
import com.example.eumbev2.dto.auth.*;
import com.example.eumbev2.entity.auth.EmailVerification;
import com.example.eumbev2.entity.auth.PasswordResetToken;
import com.example.eumbev2.entity.auth.RefreshToken;
import com.example.eumbev2.entity.user.User;
import com.example.eumbev2.repository.auth.EmailVerificationRepository;
import com.example.eumbev2.repository.auth.PasswordResetTokenRepository;
import com.example.eumbev2.repository.auth.RefreshTokenRepository;
import com.example.eumbev2.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    private final long refreshTokenValiditySeconds;
    private final long refreshTokenKeepSignedInValiditySeconds;
    private final long emailCodeExpiryMinutes;
    private final long passwordResetExpiryMinutes;

    public AuthService(
            UserRepository userRepository,
            EmailVerificationRepository emailVerificationRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            EmailService emailService,
            @Value("${jwt.refresh-token-validity-seconds}") long refreshTokenValiditySeconds,
            @Value("${jwt.refresh-token-keep-signed-in-validity-seconds}") long refreshTokenKeepSignedInValiditySeconds,
            @Value("${app.email-verification.code-expiry-minutes}") long emailCodeExpiryMinutes,
            @Value("${app.password-reset.token-expiry-minutes}") long passwordResetExpiryMinutes
    ) {
        this.userRepository = userRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
        this.refreshTokenKeepSignedInValiditySeconds = refreshTokenKeepSignedInValiditySeconds;
        this.emailCodeExpiryMinutes = emailCodeExpiryMinutes;
        this.passwordResetExpiryMinutes = passwordResetExpiryMinutes;
    }

    public AuthTokensResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(ErrorCode.USERNAME_TAKEN);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(ErrorCode.EMAIL_TAKEN);
        }
        if (!Boolean.TRUE.equals(request.privacyAgreed())) {
            throw new ApiException(ErrorCode.PRIVACY_NOT_AGREED);
        }
        boolean emailVerified = emailVerificationRepository
                .findTopByEmailAndVerifiedTrueOrderByCreatedAtDesc(request.email())
                .isPresent();
        if (!emailVerified) {
            throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.username())
                .privacyAgreed(true)
                .build();
        userRepository.save(user);

        return issueTokens(user, false);
    }

    public AuthTokensResponse signin(SigninRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }
        refreshTokenRepository.deleteByUser(user);
        return issueTokens(user, request.keepSignedInOrDefault());
    }

    public AuthTokensResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_REFRESH_TOKEN));
        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new ApiException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        User user = stored.getUser();
        refreshTokenRepository.delete(stored);
        return issueTokens(user, false);
    }

    public void signout() {
        User user = SecurityUtils.getCurrentUser();
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse checkUsernameAvailable(String username) {
        return new AvailabilityResponse(!userRepository.existsByUsername(username));
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse checkEmailAvailable(String email) {
        return new AvailabilityResponse(!userRepository.existsByEmail(email));
    }

    public void sendEmailVerificationCode(EmailSendRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(ErrorCode.EMAIL_TAKEN);
        }
        String code = CodeGenerator.numericCode(6);
        EmailVerification verification = EmailVerification.builder()
                .email(request.email())
                .code(code)
                .verified(false)
                .expiresAt(Instant.now().plusSeconds(emailCodeExpiryMinutes * 60))
                .build();
        emailVerificationRepository.save(verification);
        emailService.sendVerificationCode(request.email(), code);
    }

    public VerificationResultResponse verifyEmailCode(EmailVerifyRequest request) {
        EmailVerification verification = emailVerificationRepository
                .findTopByEmailOrderByCreatedAtDesc(request.email())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_VERIFICATION_CODE));
        if (verification.isExpired() || !verification.getCode().equals(request.code())) {
            throw new ApiException(ErrorCode.INVALID_VERIFICATION_CODE);
        }
        verification.setVerified(true);
        return new VerificationResultResponse(true);
    }

    public void requestPasswordReset(PasswordResetRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String token = CodeGenerator.opaqueToken();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .used(false)
                    .expiresAt(Instant.now().plusSeconds(passwordResetExpiryMinutes * 60))
                    .build();
            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetLink(user.getEmail(), token);
        });
        // Always respond as if it succeeded, regardless of whether the email is registered,
        // so this endpoint can't be used to probe which emails have accounts.
    }

    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_RESET_TOKEN));
        if (resetToken.isExpired() || resetToken.isUsed()) {
            throw new ApiException(ErrorCode.INVALID_RESET_TOKEN);
        }
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        resetToken.setUsed(true);
        refreshTokenRepository.deleteByUser(user);
    }

    private AuthTokensResponse issueTokens(User user, boolean keepSignedIn) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenValue = CodeGenerator.opaqueToken();
        long validitySeconds = keepSignedIn ? refreshTokenKeepSignedInValiditySeconds : refreshTokenValiditySeconds;

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(Instant.now().plusSeconds(validitySeconds))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthTokensResponse.of(accessToken, refreshTokenValue, jwtTokenProvider.getAccessTokenValiditySeconds());
    }
}
