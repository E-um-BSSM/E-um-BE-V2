package com.example.eumbev2.service.auth;

import com.example.eumbev2.repository.auth.PasswordResetTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class PasswordResetTokenCleanupScheduler {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetTokenCleanupScheduler(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Scheduled(fixedDelayString = "${app.password-reset.cleanup-fixed-delay-ms:3600000}")
    @Transactional
    public void cleanupUsedOrExpiredTokens() {
        passwordResetTokenRepository.deleteByUsedTrueOrExpiresAtBefore(Instant.now());
    }
}
