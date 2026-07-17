package com.example.eumbev2.entity.auth;

import com.example.eumbev2.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Not in the ERD; required so `/auth/refresh` and `/auth/signout` can validate/revoke a
 * specific session server-side. One row per active session; refresh rotates the token digest
 * while preserving the session metadata.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String tokenDigest;

    @Column(nullable = false, unique = true, length = 36)
    private String sessionId;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean keepSignedIn;

    @Column(length = 512)
    private String deviceInfo;

    @Column(nullable = false)
    private Instant lastUsedAt;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
