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
import es.grupo8.backend.dao.PostalCodeRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dto.StoreRequestDto;
import es.grupo8.backend.entity.ChainEntity;
import es.grupo8.backend.entity.PostalCode;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.security.AdminGuard;
import es.grupo8.backend.security.CoordinatorGuard;

class StoreControllerTest {

    private StoreController      controller;
    private StoreRepository      storeRepository;
    private ChainRepository      chainRepository;
    private PostalCodeRepository postalCodeRepository;
    private AdminGuard           adminGuard;
    private CoordinatorGuard     coordinatorGuard;

    private static final String TOKEN = "Bearer valid-token";

    @BeforeEach
    void setUp() {
        controller           = new StoreController();
        storeRepository      = mock(StoreRepository.class);
        chainRepository      = mock(ChainRepository.class);
        postalCodeRepository = mock(PostalCodeRepository.class);
        adminGuard           = mock(AdminGuard.class);
        coordinatorGuard     = mock(CoordinatorGuard.class);

        ReflectionTestUtils.setField(controller, "storeRepository",      storeRepository);
        ReflectionTestUtils.setField(controller, "chainRepository",      chainRepository);
        ReflectionTestUtils.setField(controller, "postalCodeRepository", postalCodeRepository);
        ReflectionTestUtils.setField(controller, "adminGuard",           adminGuard);
        ReflectionTestUtils.setField(controller, "coordinatorGuard",     coordinatorGuard);

        when(adminGuard.isAdmin(TOKEN)).thenReturn(true);
        when(adminGuard.extractUserId(TOKEN)).thenReturn(1);
        when(coordinatorGuard.isCoordinator(TOKEN)).thenReturn(false);
    }

    // GET LIST

    @Test
    void getStores_returnsPageForAdmin() {
        when(storeRepository.findAllByOrderByIdAsc()).thenReturn(List.of(makeStore(1, "Tienda A")));
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
        when(storeRepository.findById(1)).thenReturn(Optional.of(makeStore(1, "T1")));
        assertEquals(HttpStatus.OK, controller.getStore(TOKEN, 1).getStatusCode());
    }

    @Test
    void getStore_notFound() {
        when(storeRepository.findById(99)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, controller.getStore(TOKEN, 99).getStatusCode());
    }

    // POST
    @Test
    void createStore_happyPath() {
        when(storeRepository.save(any())).thenReturn(makeStore(5, "Nueva Tienda"));
        ResponseEntity<?> res = controller.createStore(TOKEN, req("Nueva Tienda", null, null, null));
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        verify(storeRepository).save(any(Store.class));
    }

    @Test
    void createStore_missingNameReturnsBadRequest() {
        ResponseEntity<?> res = controller.createStore(TOKEN, req(null, null, null, null));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        verify(storeRepository, never()).save(any());
    }

    @Test
    void createStore_nameTooLongReturnsBadRequest() {
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.createStore(TOKEN, req("X".repeat(256), null, null, null)).getStatusCode());
    }

    @Test
    void createStore_invalidPostalCodeReturnsBadRequest() {
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.createStore(TOKEN, req("Tienda", null, "ABC", null)).getStatusCode());
    }

    @Test
    void createStore_postalCodeNotFoundReturnsBadRequest() {
        when(postalCodeRepository.findById("28001")).thenReturn(Optional.empty());
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.createStore(TOKEN, req("Tienda", null, "28001", null)).getStatusCode());
    }

    @Test
    void createStore_chainNotFoundReturnsBadRequest() {
        when(chainRepository.findById(99)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.createStore(TOKEN, req("Tienda", null, null, 99)).getStatusCode());
    }

    @Test
    void createStore_withValidChainAndPostal() {
        PostalCode pc = new PostalCode(); pc.setPostalCode("28001");
        ChainEntity ch = new ChainEntity(); ch.setIdChain(1);

        when(postalCodeRepository.findById("28001")).thenReturn(Optional.of(pc));
        when(chainRepository.findById(1)).thenReturn(Optional.of(ch));
        when(storeRepository.save(any())).thenReturn(makeStore(7, "Tienda completa"));

        assertEquals(HttpStatus.CREATED,
                controller.createStore(TOKEN, req("Tienda completa", null, "28001", 1)).getStatusCode());
    }

    // PUT
    @Test
    void updateStore_happyPath() {
        when(storeRepository.findById(1)).thenReturn(Optional.of(makeStore(1, "Old")));
        when(storeRepository.save(any())).thenReturn(makeStore(1, "New"));
        assertEquals(HttpStatus.OK,
                controller.updateStore(TOKEN, 1, req("New", null, null, null)).getStatusCode());
    }

    @Test
    void updateStore_notFoundReturns404() {
        when(storeRepository.findById(99)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                controller.updateStore(TOKEN, 99, req("X", null, null, null)).getStatusCode());
    }

    @Test
    void deleteStore_happyPath() {
        when(storeRepository.existsById(3)).thenReturn(true);
        assertEquals(HttpStatus.OK, controller.deleteStore(TOKEN, 3).getStatusCode());
        verify(storeRepository).deleteById(3);
    }

    @Test
    void deleteStore_notFoundReturns404() {
        when(storeRepository.existsById(99)).thenReturn(false);
        assertEquals(HttpStatus.NOT_FOUND, controller.deleteStore(TOKEN, 99).getStatusCode());
        verify(storeRepository, never()).deleteById(any());
    }

    // Helpers
    private static StoreRequestDto req(String name, String address, String postalCode, Integer chainId) {
        StoreRequestDto dto = new StoreRequestDto();
        dto.setName(name);
        dto.setAddress(address);
        dto.setPostalCode(postalCode);
        dto.setChainId(chainId);
        return dto;
    }

    private static Store makeStore(int id, String name) {
        Store s = new Store();
        s.setId(id);
        s.setName(name);
        return s;
    }
}