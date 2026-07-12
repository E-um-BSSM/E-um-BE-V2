package com.example.eumbev2.repository.auth;

import com.example.eumbev2.entity.auth.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);

    Optional<EmailVerification> findTopByEmailAndVerifiedTrueOrderByCreatedAtDesc(String email);
}
