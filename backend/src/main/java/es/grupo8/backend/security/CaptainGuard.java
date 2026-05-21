package es.grupo8.backend.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import es.grupo8.backend.dao.CaptainRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class CaptainGuard {

    @Autowired
    private CaptainRepository captainRepository;

    @Value("${app.jwt.secret:change-this-secret-in-production-change-this-secret-in-production}")
    private String jwtSecret;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = buildSigningKey(jwtSecret);
    }

    public void setSigningKey(SecretKey key) {
        this.signingKey = key;
    }

    public boolean isUserCaptain(String authHeader) {
        Integer userId = extractUserId(authHeader);
        return userId != null && captainRepository.isUserCaptain(userId);
    }

    public Integer extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            String subject = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(authHeader.substring(7).trim())
                    .getPayload()
                    .getSubject();
            return subject != null ? Integer.valueOf(subject) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static SecretKey buildSigningKey(String configuredSecret) {
        try {
            byte[] decoded = Decoders.BASE64.decode(configuredSecret);
            return Keys.hmacShaKeyFor(decoded);
        } catch (RuntimeException ignored) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(configuredSecret.getBytes(StandardCharsets.UTF_8));
                return Keys.hmacShaKeyFor(hash);
            } catch (NoSuchAlgorithmException ex) {
                throw new IllegalStateException("Failed to initialize JWT key", ex);
            }
        }
    }
}
