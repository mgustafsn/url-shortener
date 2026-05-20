package com.marcus.urlshortener.util;

import java.security.SecureRandom;

/**
 * Generates short random codes for URL shortening
 * SecureRandom is used to ensure better randomness and reduce the chances of collisions
 */
public class CodeGenerator {

    private static final String ALPHABET =
            "abcdefghijklmnopqrstuvxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generates a random 6-character code using the defined alphabet
     * example: "aB3xQ7"
     */
    public static String generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
