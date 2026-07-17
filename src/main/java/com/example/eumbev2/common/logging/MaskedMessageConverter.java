package com.example.eumbev2.common.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.List;
import java.util.regex.Pattern;

public class MaskedMessageConverter extends ClassicConverter {

    private static final String MASK = "$1=***";
    private static final List<Pattern> SENSITIVE_PATTERNS = List.of(
            Pattern.compile("(?i)(authorization)\\s*[:=]\\s*(bearer\\s+)?[^\\s,;}]+"),
            Pattern.compile("(?i)(password|newPassword|accessToken|refreshToken|resetToken|token)\\s*[:=]\\s*[^\\s,;}]+"),
            Pattern.compile("(?i)(password|new_password|access_token|refresh_token|reset_token|token)\"\\s*:\\s*\"[^\"]*\"")
    );

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            message = pattern.matcher(message).replaceAll(MASK);
        }
        return message;
    }
}
