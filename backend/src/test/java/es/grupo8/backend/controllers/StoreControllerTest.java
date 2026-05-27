package es.grupo8.backend.controllers;

import java.util.List;
import java.util.Map;

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

import es.grupo8.backend.dto.StoreDTO;
import es.grupo8.backend.security.AdminGuard;
import es.grupo8.backend.security.CoordinatorGuard;
import es.grupo8.backend.services.StoreService;
import org.springframework.web.server.ResponseStatusException;

class StoreControllerTest {

    private StoreController      controller;
    private StoreService         storeService;
    private AdminGuard           adminGuard;
    private CoordinatorGuard     coordinatorGuard;

    private static final String TOKEN = "Bearer valid-token";

    @BeforeEach
    void setUp() {
        storeService         = mock(StoreService.class);
        adminGuard           = mock(AdminGuard.class);
        coordinatorGuard     = mock(CoordinatorGuard.class);

        controller           = new StoreController(storeService, adminGuard, coordinatorGuard);

        when(adminGuard.isAdmin(TOKEN)).thenReturn(true);
        when(adminGuard.extractUserId(TOKEN)).thenReturn(1);
        when(coordinatorGuard.isCoordinator(TOKEN)).thenReturn(false);
    }

    // GET LIST

    @Test
    void getStores_returnsPageForAdmin() {
        when(storeService.findAll(null, null, null, 0, 20))
            .thenReturn(Map.of("content", List.of(makeStore(1, "Tienda A")), "totalElements", 1));
        ResponseEntity<?> res = controller.getStores(TOKEN, null, null, null, 0, 20);
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    void getStores_forbiddenForNonAdmin() {
        when(adminGuard.isAdmin("Bearer x")).thenReturn(false);
        when(coordinatorGuard.isCoordinator("Bearer x")).thenReturn(false);
        assertEquals(HttpStatus.FORBIDDEN,
                controller.getStores("Bearer x", null, null, null, 0, 20).getStatusCode());
    }

    // GET ONE
    @Test
    void getStore_returnsStoreForAdmin() {
        when(storeService.findById(1)).thenReturn(makeStore(1, "T1"));
        assertEquals(HttpStatus.OK, controller.getStore(TOKEN, 1).getStatusCode());
    }

    @Test
    void getStore_notFound() {
        when(storeService.findById(99))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        assertEquals(HttpStatus.NOT_FOUND, controller.getStore(TOKEN, 99).getStatusCode());
    }

    // POST
    @Test
    void createStore_happyPath() {
        when(storeService.create(any())).thenReturn(makeStore(5, "Nueva Tienda"));
        ResponseEntity<?> res = controller.createStore(TOKEN, req("Nueva Tienda", null, null, null));
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        verify(storeService).create(any(StoreDTO.class));
    }

    @Test
    void createStore_missingNameReturnsBadRequest() {
        when(storeService.create(any())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required"));
        ResponseEntity<?> res = controller.createStore(TOKEN, req(null, null, null, null));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        verify(storeService).create(any(StoreDTO.class));
    }

    @Test
    void createStore_nameTooLongReturnsBadRequest() {
        when(storeService.create(any())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name too long"));
        assertEquals(HttpStatus.BAD_REQUEST,
            controller.createStore(TOKEN, req("X".repeat(256), null, null, null)).getStatusCode());
    }

    @Test
    void createStore_invalidPostalCodeReturnsBadRequest() {
        when(storeService.create(any())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid postal code"));
        assertEquals(HttpStatus.BAD_REQUEST,
            controller.createStore(TOKEN, req("Tienda", null, "ABC", null)).getStatusCode());
    }

    @Test
    void createStore_postalCodeNotFoundReturnsBadRequest() {
        when(storeService.create(any())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Postal code not found"));
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.createStore(TOKEN, req("Tienda", null, "28001", null)).getStatusCode());
    }

    @Test
    void createStore_chainNotFoundReturnsBadRequest() {
        when(storeService.create(any())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chain not found"));
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.createStore(TOKEN, req("Tienda", null, null, 99)).getStatusCode());
    }

    @Test
    void createStore_withValidChainAndPostal() {
        when(storeService.create(any())).thenReturn(makeStore(7, "Tienda completa"));

        assertEquals(HttpStatus.CREATED,
                controller.createStore(TOKEN, req("Tienda completa", null, "28001", 1)).getStatusCode());
    }

    // PUT
    @Test
    void updateStore_happyPath() {
        when(storeService.update(1, req("New", null, null, null))).thenReturn(makeStore(1, "New"));
        assertEquals(HttpStatus.OK,
                controller.updateStore(TOKEN, 1, req("New", null, null, null)).getStatusCode());
    }

    @Test
    void updateStore_notFoundReturns404() {
        when(storeService.update(99, req("X", null, null, null)))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        assertEquals(HttpStatus.NOT_FOUND,
                controller.updateStore(TOKEN, 99, req("X", null, null, null)).getStatusCode());
    }

    @Test
    void deleteStore_happyPath() {
        assertEquals(HttpStatus.OK, controller.deleteStore(TOKEN, 3).getStatusCode());
        verify(storeService).delete(3);
    }

    @Test
    void deleteStore_notFoundReturns404() {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"))
            .when(storeService).delete(99);
        assertEquals(HttpStatus.NOT_FOUND, controller.deleteStore(TOKEN, 99).getStatusCode());
        verify(storeService).delete(99);
    }

    // Helpers
    private static StoreDTO req(String name, String address, String postalCode, Integer chainId) {
        StoreDTO dto = new StoreDTO();
        dto.setName(name);
        dto.setAddress(address);
        dto.setPostalCode(postalCode);
        dto.setChainId(chainId);
        return dto;
    }

    private static StoreDTO makeStore(int id, String name) {
        StoreDTO dto = new StoreDTO();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}