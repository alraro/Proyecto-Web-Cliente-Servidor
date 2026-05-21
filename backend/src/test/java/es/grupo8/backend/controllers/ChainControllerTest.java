package es.grupo8.backend.controllers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import es.grupo8.backend.dao.ChainRepository;
import es.grupo8.backend.dto.ChainRequestDto;
import es.grupo8.backend.entity.ChainEntity;
import es.grupo8.backend.security.AdminGuard;

class ChainControllerTest {

    private ChainController controller;
    private ChainRepository chainRepository;
    private AdminGuard      adminGuard;

    private static final String ADMIN_TOKEN = "Bearer valid-token";

    @BeforeEach
    void setUp() {
        controller      = new ChainController();
        chainRepository = mock(ChainRepository.class);
        adminGuard      = mock(AdminGuard.class);

        ReflectionTestUtils.setField(controller, "chainRepository", chainRepository);
        ReflectionTestUtils.setField(controller, "adminGuard",      adminGuard);

        when(adminGuard.isAdmin(ADMIN_TOKEN)).thenReturn(true);
        when(adminGuard.extractUserId(ADMIN_TOKEN)).thenReturn(1);
    }

    // GET
    @Test
    void getChains_returnsListForAdmin() {
        when(chainRepository.findAll()).thenReturn(List.of(makeChain(1, "Mercadona", "MERC", true)));
        assertEquals(HttpStatus.OK, controller.getChains(ADMIN_TOKEN).getStatusCode());
    }

    @Test
    void getChains_forbiddenForNonAdmin() {
        when(adminGuard.isAdmin("Bearer bad")).thenReturn(false);
        assertEquals(HttpStatus.FORBIDDEN, controller.getChains("Bearer bad").getStatusCode());
    }

    // POST
    @Test
    void createChain_happyPath() {
        when(chainRepository.existsByCode("CARR")).thenReturn(false);
        when(chainRepository.save(any())).thenReturn(makeChain(10, "Carrefour", "CARR", false));

        ResponseEntity<?> res = controller.createChain(ADMIN_TOKEN, req("Carrefour", "CARR", false));

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        verify(chainRepository).save(any(ChainEntity.class));
    }

    @Test
    void createChain_missingNameReturnsBadRequest() {
        ResponseEntity<?> res = controller.createChain(ADMIN_TOKEN, req(null, "CARR", false));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        verify(chainRepository, never()).save(any());
    }

    @Test
    void createChain_missingCodeReturnsBadRequest() {
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.createChain(ADMIN_TOKEN, req("Carrefour", null, false)).getStatusCode());
    }

    @Test
    void createChain_invalidCodeReturnsBadRequest() {
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.createChain(ADMIN_TOKEN, req("X", "INVA LID!", false)).getStatusCode());
    }

    @Test
    void createChain_duplicateCodeReturnsConflict() {
        when(chainRepository.existsByCode("DUP")).thenReturn(true);
        assertEquals(HttpStatus.CONFLICT,
                controller.createChain(ADMIN_TOKEN, req("Duplicada", "DUP", false)).getStatusCode());
    }

    @Test
    void createChain_nameTooLongReturnsBadRequest() {
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.createChain(ADMIN_TOKEN, req("A".repeat(256), "CODE1", false)).getStatusCode());
    }

    @Test
    void createChain_forbiddenForNonAdmin() {
        when(adminGuard.isAdmin("Bearer bad")).thenReturn(false);
        assertEquals(HttpStatus.FORBIDDEN,
                controller.createChain("Bearer bad", req("X", "X", false)).getStatusCode());
    }

    // PUT
    @Test
    void updateChain_happyPath() {
        when(chainRepository.findById(1)).thenReturn(Optional.of(makeChain(1, "Old", "OLD", false)));
        when(chainRepository.existsByCode("NEW")).thenReturn(false);
        when(chainRepository.save(any())).thenReturn(makeChain(1, "New", "NEW", true));

        assertEquals(HttpStatus.OK,
                controller.updateChain(ADMIN_TOKEN, 1, req("New", "NEW", true)).getStatusCode());
    }

    @Test
    void updateChain_notFoundReturns404() {
        when(chainRepository.findById(99)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                controller.updateChain(ADMIN_TOKEN, 99, req("X", "X", false)).getStatusCode());
    }

    @Test
    void updateChain_duplicateCodeConflict() {
        when(chainRepository.findById(1)).thenReturn(Optional.of(makeChain(1, "Old", "OLD", false)));
        when(chainRepository.existsByCode("OTHER")).thenReturn(true);
        assertEquals(HttpStatus.CONFLICT,
                controller.updateChain(ADMIN_TOKEN, 1, req("Old", "OTHER", false)).getStatusCode());
    }

    @Test
    void deleteChain_happyPath() {
        when(chainRepository.existsById(5)).thenReturn(true);
        assertEquals(HttpStatus.OK, controller.deleteChain(ADMIN_TOKEN, 5).getStatusCode());
        verify(chainRepository).deleteById(5);
    }

    @Test
    void deleteChain_notFoundReturns404() {
        when(chainRepository.existsById(99)).thenReturn(false);
        assertEquals(HttpStatus.NOT_FOUND, controller.deleteChain(ADMIN_TOKEN, 99).getStatusCode());
        verify(chainRepository, never()).deleteById(any());
    }

    @Test
    void deleteChain_forbiddenForNonAdmin() {
        when(adminGuard.isAdmin(null)).thenReturn(false);
        assertEquals(HttpStatus.FORBIDDEN, controller.deleteChain(null, 1).getStatusCode());
    }

    // Helpers
    private static ChainRequestDto req(String name, String code, boolean participation) {
        ChainRequestDto dto = new ChainRequestDto();
        dto.setName(name);
        dto.setCode(code);
        dto.setParticipation(participation);
        return dto;
    }

    private static ChainEntity makeChain(int id, String name, String code, boolean participation) {
        ChainEntity c = new ChainEntity();
        c.setIdChain(id);
        c.setName(name);
        c.setCode(code);
        c.setParticipation(participation);
        return c;
    }
}