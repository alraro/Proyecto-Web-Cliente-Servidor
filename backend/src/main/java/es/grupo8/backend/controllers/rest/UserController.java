/**
 * REST controller for user management.
 *
 * Authors:
 * - Alfonso Ramos: 40%
 * - Alejandra Ortiz: 40%
 * - Generative AI: 20%
 */
package es.grupo8.backend.controllers.rest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dto.UserResponseDto;
import es.grupo8.backend.dto.UserRoleRequestDto;
import es.grupo8.backend.dto.UserRoleResponseDto;
import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private AuthService authService;
    @Autowired private UserService userService;

    private void checkAdmin(String authHeader) {
        Integer userId = authService.extractUserIdFromToken(authHeader);
        if (userId == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token invalido o ausente");
        }
        if (!userService.isAdmin(userId)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "No tienes permiso");
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        checkAdmin(authHeader);
        List<UserResponseDto> users = userService.getAllUsersOrdered();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<UserResponseDto>> getPendingUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        checkAdmin(authHeader);
        List<UserResponseDto> pending = userService.getPendingUsersOrdered();
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/{id}/role")
    public ResponseEntity<UserRoleResponseDto> assignRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) UserRoleRequestDto request) {

        checkAdmin(authHeader);
        UserRoleResponseDto response = userService.assignRole(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        checkAdmin(authHeader);
        Integer adminId = authService.extractUserIdFromToken(authHeader);
        if (id.equals(adminId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "No puedes eliminar tu propia cuenta"));
        }

        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente"));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(AuthException e) {
        return ResponseEntity.status(e.getStatus())
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
    }
}
