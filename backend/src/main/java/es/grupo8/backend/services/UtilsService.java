package es.grupo8.backend.services;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.regex.Pattern;

public final class UtilsService {

    public static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9()\\-\\s]{7,20}$");

    private UtilsService() {
    }

    public static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public static boolean isValidPhone(String telefono) {
        return telefono != null && telefono.matches("^[0-9+\\-\\s]{7,20}$");
    }

    public static boolean isValidPostalCode(String cp) {
        return cp != null && cp.matches("^[0-9]{5}$");
    }

    public static String normalizePhone(String phone) {
        String trimmed = trimToNull(phone);
        if (trimmed == null) return null;
        return trimmed.replaceAll("\\s+", " ");
    }

    public static String hashPassword(String rawPassword) {
        if (rawPassword == null) return null;
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
    }

    public static boolean matchesPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) return false;

        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return BCrypt.checkpw(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }

    public static boolean needsMigration(String storedPassword) {
        if (storedPassword == null) return false;
        return !(storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$"));
    }
}
