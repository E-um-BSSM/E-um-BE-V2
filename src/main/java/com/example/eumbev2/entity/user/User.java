package com.example.eumbev2.entity.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Maps ERD `users` table. The API spec exposes this as `UserSummary` (user_id, username,
 * nickname, avatar_url) wherever an author/mentor/mentee needs to be represented.
 *
 * Note: the API's SignupRequest does not collect a nickname, so it defaults to the
 * username at signup time. There is no profile-edit endpoint in this MVP spec.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    private String avatarUrl;

    @Column(nullable = false)
    private boolean privacyAgreed;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;
}
