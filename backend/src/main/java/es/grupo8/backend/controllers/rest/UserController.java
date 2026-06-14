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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.dto.UserResponseDto;
import es.grupo8.backend.dto.UserRoleRequestDto;
import es.grupo8.backend.dto.UserRoleResponseDto;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController extends BaseRestController {

    private final AuthService authService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<UserResponseDto>> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role) {

        authService.checkAdmin(authHeader);
        return ResponseEntity.ok(userService.getAllUsers(page, size, sort, search, role));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<UserResponseDto>> getPendingUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        authService.checkAdmin(authHeader);
        List<UserResponseDto> pending = userService.getPendingUsersOrdered();
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/{id}/role")
    public ResponseEntity<UserRoleResponseDto> assignRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) UserRoleRequestDto request) {

        authService.checkAdmin(authHeader);
        UserRoleResponseDto response = userService.assignRole(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        authService.checkAdmin(authHeader);
        Integer adminId = authService.extractUserIdFromToken(authHeader);
        if (id.equals(adminId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "No puedes eliminar tu propia cuenta"));
        }

        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente"));
    }
}
