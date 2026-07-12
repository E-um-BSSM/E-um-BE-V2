package com.example.eumbev2.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends verification codes / password reset links by email. Failures are logged rather than
 * thrown: in local/dev environments without real SMTP credentials configured, the verification
 * code is still persisted (see {@link AuthService}) so the flow remains testable end-to-end.
 */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String frontendBaseUrl;

    public EmailService(JavaMailSender mailSender, @Value("${app.frontend-base-url}") String frontendBaseUrl) {
        this.mailSender = mailSender;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public void sendVerificationCode(String email, String code) {
        send(email, "[이음] 이메일 인증코드", "인증코드: " + code + "\n10분 이내에 입력해주세요.");
    }

    public void sendPasswordResetLink(String email, String token) {
        String link = frontendBaseUrl + "/reset-password?token=" + token;
        send(email, "[이음] 비밀번호 재설정", "아래 링크에서 비밀번호를 재설정하세요:\n" + link);
    }

    private void send(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (MailException e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
