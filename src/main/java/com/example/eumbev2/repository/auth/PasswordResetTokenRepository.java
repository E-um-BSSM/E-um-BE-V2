package com.example.eumbev2.repository.auth;

import com.example.eumbev2.entity.auth.PasswordResetToken;
import com.example.eumbev2.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenDigest(String tokenDigest);

    @Modifying(flushAutomatically = true)
    @Query("update PasswordResetToken token set token.used = true where token.user = :user and token.used = false")
    int markUnusedTokensUsedByUser(@Param("user") User user);

    long deleteByUsedTrueOrExpiresAtBefore(Instant now);
}
