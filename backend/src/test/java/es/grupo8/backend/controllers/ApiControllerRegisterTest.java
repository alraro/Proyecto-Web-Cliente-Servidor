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
        request.put("postalCode", "");

        ResponseEntity<?> response = controller.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerRejectsInvalidEmail() {
        Map<String, String> request = validRequest();
        request.put("email", "invalid-email");

        ResponseEntity<?> response = controller.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerRejectsInvalidPhoneAndPostalCode() {
        Map<String, String> request = validRequest();
        request.put("phone", "abc");

        ResponseEntity<?> response = controller.register(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, String> request2 = validRequest();
        request2.put("postalCode", "29A01");
        ResponseEntity<?> response2 = controller.register(request2);
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    void registerRejectsDuplicatedEmail() {
        when(userRepository.existsByEmail("user@bancosol.org")).thenReturn(true);

        ResponseEntity<?> response = controller.register(validRequest());

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerCreatesUserWithHashedPasswordAndReturnsCreated() {
        when(userRepository.existsByEmail("user@bancosol.org")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setIdUser(99);
            return user;
        });

        ResponseEntity<?> response = controller.register(validRequest());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertNotEquals("password123", saved.getPassword());
        assertTrue(saved.getPassword().startsWith("$2"));

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Registration successful", body.get("message"));
    }

    @Test
    void loginMigratesLegacyPassword() {
        UserEntity legacy = new UserEntity();
        legacy.setIdUser(1);
        legacy.setName("Legacy User");
        legacy.setEmail("legacy@bancosol.org");
        legacy.setPassword("legacy123");

        when(userRepository.findByEmail("legacy@bancosol.org")).thenReturn(Optional.of(legacy));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, String> req = new HashMap<>();
        req.put("email", "legacy@bancosol.org");
        req.put("password", "legacy123");

        ResponseEntity<?> response = controller.login(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(any(UserEntity.class));
    }

    private Map<String, String> validRequest() {
        Map<String, String> request = new HashMap<>();
        request.put("name", "Test User");
        request.put("email", "user@bancosol.org");
        request.put("phone", "600123123");
        request.put("password", "password123");
        request.put("address", "Main Street 1");
        request.put("postalCode", "29001");
        return request;
    }
}
