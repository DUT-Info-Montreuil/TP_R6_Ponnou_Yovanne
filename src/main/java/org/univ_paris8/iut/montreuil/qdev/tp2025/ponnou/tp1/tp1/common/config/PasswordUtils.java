package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtils() {
    }

    public static String hash(String password) {
        return ENCODER.encode(password);
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        return ENCODER.matches(rawPassword, hashedPassword);
    }
}
