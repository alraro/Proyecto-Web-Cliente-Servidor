package es.grupo8.backend.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordServiceTest {

    private final PasswordService passwordService = new PasswordService();

    @Test
    void hashAndMatchWorks() {
        String raw = "contrasenaSegura123";
        String hashed = passwordService.hash(raw);

        assertNotEquals(raw, hashed);
        assertTrue(passwordService.matches(raw, hashed));
        assertFalse(passwordService.matches("otra", hashed));
        assertFalse(passwordService.needsMigration(hashed));
    }

    @Test
    void legacyPlainPasswordStillMatchesForMigration() {
        String raw = "legacy";

        assertTrue(passwordService.matches(raw, raw));
        assertTrue(passwordService.needsMigration(raw));
    }
}
