package es.grupo8.backend.controllers;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import es.grupo8.backend.dto.LoginResponseDTO;
import es.grupo8.backend.dto.RegisterResponseDTO;
import es.grupo8.backend.services.AuthService;

class ApiControllerRegisterTest {

    private AuthController controller;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        controller = new AuthController();
        authService = mock(AuthService.class);

        ReflectionTestUtils.setField(controller, "authService", authService);
    }

    @Test
    void registerRejectsMissingFields() {
        Map<String, String> request = validRequest();
        request.put("cp", "");

        ResponseEntity<?> response = controller.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(authService);
    }

    @Test
    void registerRejectsInvalidEmail() {
        Map<String, String> request = validRequest();
        request.put("email", "invalid-email");

        ResponseEntity<?> response = controller.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(authService);
    }

    @Test
    void registerRejectsInvalidPhoneAndPostalCode() {
        Map<String, String> request = validRequest();
        request.put("telefono", "abc");

        ResponseEntity<?> response = controller.register(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, String> request2 = validRequest();
        request2.put("cp", "29A01");
        ResponseEntity<?> response2 = controller.register(request2);
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    void registerRejectsDuplicatedEmail() {
        when(authService.emailExists("user@bancosol.org")).thenReturn(true);

        ResponseEntity<?> response = controller.register(validRequest());

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(authService, never()).register(any(), any(), any(), any(), any(), any());
    }

    @Test
    void registerCreatesUserWithHashedPasswordAndReturnsCreated() {
        when(authService.emailExists("user@bancosol.org")).thenReturn(false);

        RegisterResponseDTO dto = new RegisterResponseDTO();
        dto.setMessage("Registro correcto");
        when(authService.register("Test User", "user@bancosol.org", "password123", "600123123", "Main Street 1", "29001"))
                .thenReturn(dto);

        ResponseEntity<?> response = controller.register(validRequest());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Registro correcto", body.get("message"));
        verify(authService).register("Test User", "user@bancosol.org", "password123", "600123123", "Main Street 1", "29001");
    }

    @Test
    void loginReturnsForbiddenWhenRoleIsPending() {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setRole("PENDIENTE");
        when(authService.login("legacy@bancosol.org", "legacy123")).thenReturn(dto);

        Map<String, String> req = new HashMap<>();
        req.put("email", "legacy@bancosol.org");
        req.put("password", "legacy123");

        ResponseEntity<?> response = controller.login(req);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    private Map<String, String> validRequest() {
        Map<String, String> request = new HashMap<>();
        request.put("nombre", "Test User");
        request.put("email", "user@bancosol.org");
        request.put("telefono", "600123123");
        request.put("password", "password123");
        request.put("domicilio", "Main Street 1");
        request.put("cp", "29001");
        return request;
    }
}
