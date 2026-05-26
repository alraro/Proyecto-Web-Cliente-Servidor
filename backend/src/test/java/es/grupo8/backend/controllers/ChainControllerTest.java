package es.grupo8.backend.controllers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import es.grupo8.backend.dto.ChainDTO;
import es.grupo8.backend.security.AdminGuard;
import es.grupo8.backend.services.ChainService;
import org.springframework.web.server.ResponseStatusException;

class ChainControllerTest {

    private ChainController controller;
    private ChainService    chainService;
    private AdminGuard      adminGuard;

    private static final String ADMIN_TOKEN = "Bearer valid-token";

    @BeforeEach
    void setUp() {
        chainService    = mock(ChainService.class);
        adminGuard      = mock(AdminGuard.class);

        controller      = new ChainController(chainService, adminGuard);

        when(adminGuard.isAdmin(ADMIN_TOKEN)).thenReturn(true);
        when(adminGuard.extractUserId(ADMIN_TOKEN)).thenReturn(1);
    }

    // GET
    @Test
    void getChains_returnsListForAdmin() {
        when(chainService.findAll()).thenReturn(List.of(makeChain(1, "Mercadona", "MERC", true)));
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
        when(chainService.create(any())).thenReturn(makeChain(10, "Carrefour", "CARR", false));

        ResponseEntity<?> res = controller.createChain(ADMIN_TOKEN, req("Carrefour", "CARR", false));

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        verify(chainService).create(any(ChainDTO.class));
    }

    @Test
    void createChain_missingNameReturnsBadRequest() {
        when(chainService.create(any())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required"));
        ResponseEntity<?> res = controller.createChain(ADMIN_TOKEN, req(null, "CARR", false));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        verify(chainService).create(any(ChainDTO.class));
    }

    @Test
    void createChain_missingCodeReturnsBadRequest() {
        when(chainService.create(any())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code is required"));
        assertEquals(HttpStatus.BAD_REQUEST,
            controller.createChain(ADMIN_TOKEN, req("Carrefour", null, false)).getStatusCode());
    }

    @Test
    void createChain_invalidCodeReturnsBadRequest() {
        when(chainService.create(any())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code"));
        assertEquals(HttpStatus.BAD_REQUEST,
            controller.createChain(ADMIN_TOKEN, req("X", "INVA LID!", false)).getStatusCode());
    }

    @Test
    void createChain_duplicateCodeReturnsConflict() {
        when(chainService.create(any())).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate"));
        assertEquals(HttpStatus.CONFLICT,
                controller.createChain(ADMIN_TOKEN, req("Duplicada", "DUP", false)).getStatusCode());
    }

    @Test
    void createChain_nameTooLongReturnsBadRequest() {
        when(chainService.create(any())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name too long"));
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
        when(chainService.update(1, req("New", "NEW", true))).thenReturn(makeChain(1, "New", "NEW", true));

        assertEquals(HttpStatus.OK,
                controller.updateChain(ADMIN_TOKEN, 1, req("New", "NEW", true)).getStatusCode());
    }

    @Test
    void updateChain_notFoundReturns404() {
        when(chainService.update(99, req("X", "X", false)))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Chain not found"));
        assertEquals(HttpStatus.NOT_FOUND,
                controller.updateChain(ADMIN_TOKEN, 99, req("X", "X", false)).getStatusCode());
    }

    @Test
    void updateChain_duplicateCodeConflict() {
        when(chainService.update(1, req("Old", "OTHER", false)))
            .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate"));
        assertEquals(HttpStatus.CONFLICT,
                controller.updateChain(ADMIN_TOKEN, 1, req("Old", "OTHER", false)).getStatusCode());
    }

    @Test
    void deleteChain_happyPath() {
        assertEquals(HttpStatus.OK, controller.deleteChain(ADMIN_TOKEN, 5).getStatusCode());
        verify(chainService).delete(5);
    }

    @Test
    void deleteChain_notFoundReturns404() {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Chain not found"))
            .when(chainService).delete(99);
        assertEquals(HttpStatus.NOT_FOUND, controller.deleteChain(ADMIN_TOKEN, 99).getStatusCode());
        verify(chainService).delete(99);
    }

    @Test
    void deleteChain_forbiddenForNonAdmin() {
        when(adminGuard.isAdmin(null)).thenReturn(false);
        assertEquals(HttpStatus.FORBIDDEN, controller.deleteChain(null, 1).getStatusCode());
    }

    // Helpers
    private static ChainDTO req(String name, String code, boolean participation) {
        ChainDTO dto = new ChainDTO();
        dto.setName(name);
        dto.setCode(code);
        dto.setParticipation(participation);
        return dto;
    }

    private static ChainDTO makeChain(int id, String name, String code, boolean participation) {
        ChainDTO dto = new ChainDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setCode(code);
        dto.setParticipation(participation);
        return dto;
    }
}