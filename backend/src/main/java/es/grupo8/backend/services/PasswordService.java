package es.grupo8.backend.services;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class PasswordService {

    public String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
    }

    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return BCrypt.checkpw(rawPassword, storedPassword);
        }

        return rawPassword.equals(storedPassword);
    }

    public boolean needsMigration(String storedPassword) {
        if (storedPassword == null) {
            return true;
        }

        return !(storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$"));
    }
}
