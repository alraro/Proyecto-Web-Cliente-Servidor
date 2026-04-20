package es.grupo8.backend.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.entity.UserEntity;

class ApiControllerRegisterTest {

    private ApiController controller;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        controller = new ApiController();
        userRepository = mock(UserRepository.class);

        ReflectionTestUtils.setField(controller, "userRepository", userRepository);
        ReflectionTestUtils.setField(controller, "jwtSecret", "change-this-secret-in-production-change-this-secret-in-production");
        ReflectionTestUtils.setField(controller, "jwtExpirationMs", 7200000L);
        controller.initJwt();
    }

    @Test
    void registerRejectsMissingFields() {
        Map<String, String> request = validRequest();
        request.put("cp", "");

        ResponseEntity<?> response = controller.register(request, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerRejectsInvalidEmail() {
        Map<String, String> request = validRequest();
        request.put("email", "correo-invalido");

        ResponseEntity<?> response = controller.register(request, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerRejectsInvalidPhoneAndPostalCode() {
        Map<String, String> request = validRequest();
        request.put("telefono", "abc");

        ResponseEntity<?> response = controller.register(request, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, String> request2 = validRequest();
        request2.put("cp", "29A01");
        ResponseEntity<?> response2 = controller.register(request2, null);
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    void registerRejectsDuplicatedEmail() {
        when(userRepository.existsByEmail("usuario@bancosol.org")).thenReturn(true);

        ResponseEntity<?> response = controller.register(validRequest(), null);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerCreatesUserWithHashedPasswordAndReturnsCreated() {
        when(userRepository.existsByEmail("usuario@bancosol.org")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setIdUsuario(99);
            return user;
        });

        ResponseEntity<?> response = controller.register(validRequest(), null);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertNotEquals("clave123", saved.getContrasena());
        assertTrue(saved.getContrasena().startsWith("$2"));

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Registro correcto", body.get("message"));
    }

    @Test
    void loginMigratesLegacyPassword() {
        UserEntity legacy = new UserEntity();
        legacy.setIdUsuario(1);
        legacy.setNombre("Usuario Legacy");
        legacy.setEmail("legacy@bancosol.org");
        legacy.setContrasena("legacy123");

        when(userRepository.findByEmail("legacy@bancosol.org")).thenReturn(Optional.of(legacy));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, String> req = new HashMap<>();
        req.put("email", "legacy@bancosol.org");
        req.put("password", "legacy123");

        ResponseEntity<?> response = controller.login(req, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(any(UserEntity.class));
    }

    private Map<String, String> validRequest() {
        Map<String, String> request = new HashMap<>();
        request.put("nombre", "Usuario Prueba");
        request.put("email", "usuario@bancosol.org");
        request.put("telefono", "600123123");
        request.put("password", "clave123");
        request.put("domicilio", "Calle Larios 1");
        request.put("cp", "29001");
        return request;
    }
}
