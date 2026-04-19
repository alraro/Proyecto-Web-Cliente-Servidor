package es.grupo8.backend.services;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CsrfService {

    private final long expirationMs;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Long> tokenExpirations = new ConcurrentHashMap<>();

    public CsrfService(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public String generateToken() {
        byte[] buffer = new byte[32];
        random.nextBytes(buffer);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
        tokenExpirations.put(token, Instant.now().toEpochMilli() + expirationMs);
        return token;
    }

    public boolean validateAndConsume(String token) {
        if (token == null) {
            return false;
        }

        Long expiresAt = tokenExpirations.remove(token);
        if (expiresAt == null) {
            return false;
        }

        return Instant.now().toEpochMilli() <= expiresAt;
    }
}
