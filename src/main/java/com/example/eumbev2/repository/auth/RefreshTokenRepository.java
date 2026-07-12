package com.example.eumbev2.repository.auth;

import com.example.eumbev2.entity.auth.RefreshToken;
import com.example.eumbev2.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
