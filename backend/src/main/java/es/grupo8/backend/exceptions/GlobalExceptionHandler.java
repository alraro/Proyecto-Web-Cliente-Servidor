package es.grupo8.backend.exceptions;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST controllers.
 * Catches {@link AuthException} thrown by checkAuth() helpers and returns the appropriate HTTP status.
 * This centralises auth error responses so individual controllers stay clean.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles authentication and authorisation failures.
     *
     * @param e the AuthException thrown by a controller's checkAuth helper
     * @return response with the HTTP status stored in the exception and a JSON message body
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(AuthException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
    }
}
