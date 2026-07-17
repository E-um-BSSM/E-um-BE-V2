package com.example.eumbev2.service.auth;

import com.example.eumbev2.common.exception.ApiException;
import com.example.eumbev2.common.exception.ErrorCode;
import com.example.eumbev2.common.util.TokenDigest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthRateLimitService {

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private final Map<String, Instant> cooldowns = new ConcurrentHashMap<>();

    private final int signinIpLimit;
    private final int signinUsernameLimit;
    private final int signinFailedUsernameLimit;
    private final Duration signinWindow;
    private final int emailSendIpLimit;
    private final int emailSendEmailLimit;
    private final Duration emailSendWindow;
    private final Duration emailSendCooldown;
    private final int emailVerifyIpLimit;
    private final int emailVerifyEmailLimit;
    private final int emailVerifyFailedEmailLimit;
    private final Duration emailVerifyWindow;
    private final int passwordResetRequestIpLimit;
    private final int passwordResetRequestEmailLimit;
    private final Duration passwordResetRequestWindow;
    private final Duration passwordResetRequestCooldown;
    private final int passwordResetIpLimit;
    private final int passwordResetTokenLimit;
    private final Duration passwordResetWindow;
    private final int refreshIpLimit;
    private final int refreshTokenLimit;
    private final Duration refreshWindow;

    public AuthRateLimitService(
            @Value("${app.rate-limit.signin.ip-limit:20}") int signinIpLimit,
            @Value("${app.rate-limit.signin.username-limit:10}") int signinUsernameLimit,
            @Value("${app.rate-limit.signin.failed-username-limit:5}") int signinFailedUsernameLimit,
            @Value("${app.rate-limit.signin.window-seconds:900}") long signinWindowSeconds,
            @Value("${app.rate-limit.email-send.ip-limit:10}") int emailSendIpLimit,
            @Value("${app.rate-limit.email-send.email-limit:3}") int emailSendEmailLimit,
            @Value("${app.rate-limit.email-send.window-seconds:3600}") long emailSendWindowSeconds,
            @Value("${app.rate-limit.email-send.cooldown-seconds:60}") long emailSendCooldownSeconds,
            @Value("${app.rate-limit.email-verify.ip-limit:30}") int emailVerifyIpLimit,
            @Value("${app.rate-limit.email-verify.email-limit:10}") int emailVerifyEmailLimit,
            @Value("${app.rate-limit.email-verify.failed-email-limit:5}") int emailVerifyFailedEmailLimit,
            @Value("${app.rate-limit.email-verify.window-seconds:900}") long emailVerifyWindowSeconds,
            @Value("${app.rate-limit.password-reset-request.ip-limit:10}") int passwordResetRequestIpLimit,
            @Value("${app.rate-limit.password-reset-request.email-limit:3}") int passwordResetRequestEmailLimit,
            @Value("${app.rate-limit.password-reset-request.window-seconds:3600}") long passwordResetRequestWindowSeconds,
            @Value("${app.rate-limit.password-reset-request.cooldown-seconds:60}") long passwordResetRequestCooldownSeconds,
            @Value("${app.rate-limit.password-reset.ip-limit:20}") int passwordResetIpLimit,
            @Value("${app.rate-limit.password-reset.token-limit:5}") int passwordResetTokenLimit,
            @Value("${app.rate-limit.password-reset.window-seconds:900}") long passwordResetWindowSeconds,
            @Value("${app.rate-limit.refresh.ip-limit:60}") int refreshIpLimit,
            @Value("${app.rate-limit.refresh.token-limit:10}") int refreshTokenLimit,
            @Value("${app.rate-limit.refresh.window-seconds:900}") long refreshWindowSeconds
    ) {
        this.signinIpLimit = signinIpLimit;
        this.signinUsernameLimit = signinUsernameLimit;
        this.signinFailedUsernameLimit = signinFailedUsernameLimit;
        this.signinWindow = Duration.ofSeconds(signinWindowSeconds);
        this.emailSendIpLimit = emailSendIpLimit;
        this.emailSendEmailLimit = emailSendEmailLimit;
        this.emailSendWindow = Duration.ofSeconds(emailSendWindowSeconds);
        this.emailSendCooldown = Duration.ofSeconds(emailSendCooldownSeconds);
        this.emailVerifyIpLimit = emailVerifyIpLimit;
        this.emailVerifyEmailLimit = emailVerifyEmailLimit;
        this.emailVerifyFailedEmailLimit = emailVerifyFailedEmailLimit;
        this.emailVerifyWindow = Duration.ofSeconds(emailVerifyWindowSeconds);
        this.passwordResetRequestIpLimit = passwordResetRequestIpLimit;
        this.passwordResetRequestEmailLimit = passwordResetRequestEmailLimit;
        this.passwordResetRequestWindow = Duration.ofSeconds(passwordResetRequestWindowSeconds);
        this.passwordResetRequestCooldown = Duration.ofSeconds(passwordResetRequestCooldownSeconds);
        this.passwordResetIpLimit = passwordResetIpLimit;
        this.passwordResetTokenLimit = passwordResetTokenLimit;
        this.passwordResetWindow = Duration.ofSeconds(passwordResetWindowSeconds);
        this.refreshIpLimit = refreshIpLimit;
        this.refreshTokenLimit = refreshTokenLimit;
        this.refreshWindow = Duration.ofSeconds(refreshWindowSeconds);
    }

    public void checkSignin(HttpServletRequest request, String username) {
        String ip = clientIp(request);
        String normalizedUsername = normalize(username);
        consume("signin:ip:" + ip, signinIpLimit, signinWindow);
        consume("signin:username:" + normalizedUsername, signinUsernameLimit, signinWindow);
        ensureBelowLimit("signin:failed-username:" + normalizedUsername, signinFailedUsernameLimit, signinWindow);
    }

    public void recordSigninFailure(String username) {
        consume("signin:failed-username:" + normalize(username), signinFailedUsernameLimit, signinWindow);
    }

    public void clearSigninFailures(String username) {
        counters.remove("signin:failed-username:" + normalize(username));
    }

    public void checkEmailSend(HttpServletRequest request, String email) {
        String ip = clientIp(request);
        String normalizedEmail = normalize(email);
        ensureCooldownElapsed("email-send:cooldown:" + normalizedEmail);
        consume("email-send:ip:" + ip, emailSendIpLimit, emailSendWindow);
        consume("email-send:email:" + normalizedEmail, emailSendEmailLimit, emailSendWindow);
        cooldowns.put("email-send:cooldown:" + normalizedEmail, Instant.now().plus(emailSendCooldown));
    }

    public void checkEmailVerify(HttpServletRequest request, String email) {
        String ip = clientIp(request);
        String normalizedEmail = normalize(email);
        consume("email-verify:ip:" + ip, emailVerifyIpLimit, emailVerifyWindow);
        consume("email-verify:email:" + normalizedEmail, emailVerifyEmailLimit, emailVerifyWindow);
        ensureBelowLimit("email-verify:failed-email:" + normalizedEmail, emailVerifyFailedEmailLimit, emailVerifyWindow);
    }

    public void recordEmailVerifyFailure(String email) {
        consume("email-verify:failed-email:" + normalize(email), emailVerifyFailedEmailLimit, emailVerifyWindow);
    }

    public void clearEmailVerifyFailures(String email) {
        counters.remove("email-verify:failed-email:" + normalize(email));
    }

    public void checkPasswordResetRequest(HttpServletRequest request, String email) {
        String ip = clientIp(request);
        String normalizedEmail = normalize(email);
        ensureCooldownElapsed("password-reset-request:cooldown:" + normalizedEmail);
        consume("password-reset-request:ip:" + ip, passwordResetRequestIpLimit, passwordResetRequestWindow);
        consume("password-reset-request:email:" + normalizedEmail, passwordResetRequestEmailLimit, passwordResetRequestWindow);
        cooldowns.put("password-reset-request:cooldown:" + normalizedEmail, Instant.now().plus(passwordResetRequestCooldown));
    }

    public void checkPasswordReset(HttpServletRequest request, String token) {
        String ip = clientIp(request);
        consume("password-reset:ip:" + ip, passwordResetIpLimit, passwordResetWindow);
        consume("password-reset:token:" + TokenDigest.sha256(token), passwordResetTokenLimit, passwordResetWindow);
    }

    public void checkRefresh(HttpServletRequest request, String refreshToken) {
        String ip = clientIp(request);
        consume("refresh:ip:" + ip, refreshIpLimit, refreshWindow);
        consume("refresh:token:" + TokenDigest.sha256(refreshToken), refreshTokenLimit, refreshWindow);
    }

    @Scheduled(fixedDelayString = "${app.rate-limit.cleanup-fixed-delay-ms:300000}")
    public void cleanupExpiredEntries() {
        Instant now = Instant.now();
        counters.entrySet().removeIf(entry -> !entry.getValue().activeAt(now));
        cooldowns.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
    }

    private void consume(String key, int limit, Duration window) {
        if (limit <= 0) {
            throw new ApiException(ErrorCode.TOO_MANY_REQUESTS);
        }
        Instant now = Instant.now();
        WindowCounter counter = counters.compute(key, (ignored, current) -> {
            if (current == null || !current.activeAt(now)) {
                return new WindowCounter(1, now.plus(window));
            }
            return current.increment();
        });
        if (counter.count() > limit) {
            throw new ApiException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    private void ensureBelowLimit(String key, int limit, Duration window) {
        WindowCounter counter = counters.get(key);
        if (counter != null && counter.activeAt(Instant.now()) && counter.count() >= limit) {
            throw new ApiException(ErrorCode.TOO_MANY_REQUESTS);
        }
        if (counter != null && !counter.activeAt(Instant.now())) {
            counters.remove(key, counter);
        }
    }

    private void ensureCooldownElapsed(String key) {
        Instant blockedUntil = cooldowns.get(key);
        Instant now = Instant.now();
        if (blockedUntil != null && blockedUntil.isAfter(now)) {
            throw new ApiException(ErrorCode.TOO_MANY_REQUESTS);
        }
        if (blockedUntil != null) {
            cooldowns.remove(key, blockedUntil);
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record WindowCounter(int count, Instant resetAt) {

        WindowCounter increment() {
            return new WindowCounter(count + 1, resetAt);
        }

        boolean activeAt(Instant now) {
            return resetAt.isAfter(now);
        }
    }
}
