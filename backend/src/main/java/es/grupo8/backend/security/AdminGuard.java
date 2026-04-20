package es.grupo8.backend.security;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.grupo8.backend.dao.AdminRepository;
import io.jsonwebtoken.Jwts;

@Component
public class AdminGuard {

    @Autowired
    private AdminRepository adminRepository;

    // La clave se inyecta desde ApiController tras el @PostConstruct
    private SecretKey signingKey;

    public void setSigningKey(SecretKey key) {
        this.signingKey = key;
    }

    public boolean isAdmin(String authHeader) {
        Integer userId = extractUserId(authHeader);
        return userId != null && adminRepository.existsByIdUsuario(userId);
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
}