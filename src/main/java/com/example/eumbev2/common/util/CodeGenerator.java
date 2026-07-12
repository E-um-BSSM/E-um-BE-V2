package com.example.eumbev2.common.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Random code/token generation used across auth (email verification codes,
 * password reset tokens, refresh tokens) and classroom invite codes.
 */
public final class CodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALNUM = "abcdefghijklmnopqrstuvwxyz0123456789";

    private CodeGenerator() {
    }

    /** 6-digit numeric code, e.g. for email verification. */
    public static String numericCode(int digits) {
        StringBuilder sb = new StringBuilder(digits);
        for (int i = 0; i < digits; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /** URL-safe opaque token, e.g. for refresh tokens / password reset tokens. */
    public static String opaqueToken() {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Short lowercase-alphanumeric code, e.g. classroom invite codes. */
    public static String inviteCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALNUM.charAt(RANDOM.nextInt(ALNUM.length())));
        }
        return sb.toString();
    }
}
