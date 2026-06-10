---
name: spring-boot-auth
description: Authentication patterns for REST controllers (checkAuth, AuthException, @ExceptionHandler)
---

## Auth patterns used in this project

### AuthException (`exceptions/AuthException.java`)

Custom exception with `HttpStatus`:

```java
public class AuthException extends RuntimeException {
    private final HttpStatus status;
    public AuthException(HttpStatus status, String message) { ... }
    public HttpStatus getStatus() { return status; }
}
```

### checkAuth() methods in controllers

Three variants depending on permissions needed:

```java
// Admin only
private void checkAdmin(String auth) {
    Integer userId = authService.extractUserIdFromToken(auth);
    if (userId == null)
        throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
    if (!userService.isAdmin(userId))
        throw new AuthException(HttpStatus.FORBIDDEN, "No tienes permiso");
}

// Admin OR manager of a specific entity
private void checkAdminOrEntityManager(String auth, Integer entityId) {
    Integer userId = authService.extractUserIdFromToken(auth);
    if (userId == null)
        throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
    if (!userService.isAdmin(userId) && !userService.isManagerOfEntity(userId, entityId))
        throw new AuthException(HttpStatus.FORBIDDEN, "No tienes permiso");
}

// Partner entity manager only
private Integer checkPartnerEntityManager(String auth) {
    Integer userId = authService.extractUserIdFromToken(auth);
    if (userId == null)
        throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
    if (!userService.isPartnerEntityManager(userId))
        throw new AuthException(HttpStatus.FORBIDDEN, "No eres responsable de entidad colaboradora");
    return userId;
}
```

### @RequestHeader

Always use `required = false` so missing header returns 401 JSON instead of 400 HTML:

```java
@RequestHeader(value = "Authorization", required = false) String auth
```

### @ExceptionHandler in controller

```java
@ExceptionHandler(AuthException.class)
public ResponseEntity<Map<String, String>> handleAuthException(AuthException e) {
    return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
}
```

### Available services

| Method | What it does |
|---|---|
| `authService.extractUserIdFromToken(auth)` | Extracts userId from JWT, returns null if invalid |
| `userService.isAdmin(userId)` | Checks if user is administrator |
| `userService.isCoordinator(userId)` | Checks if user is coordinator |
| `userService.isCaptain(userId)` | Checks if user is captain |
| `userService.isPartnerEntityManager(userId)` | Checks if user is partner entity manager |
| `userService.isManagerOfEntity(userId, entityId)` | Checks if user manages a specific entity |
