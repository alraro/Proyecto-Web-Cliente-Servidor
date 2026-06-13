/**
 * Pruebas unitarias del servicio de solicitudes de alta de capitanes.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 85%
 * - IA Generativa: 15%
 */
package es.grupo8.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CaptainRequestRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.Captain;
import es.grupo8.backend.entity.CaptainRequest;
import es.grupo8.backend.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
class AdminCaptainRequestServiceTest {

    @Mock CaptainRequestRepository captainRequestRepository;
    @Mock UserRepository userRepository;
    @Mock CaptainRepository captainRepository;

    @InjectMocks AdminCaptainRequestService service;

    private CaptainRequest pendingRequest(int campaignId) {
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        CaptainRequest req = new CaptainRequest();
        req.setId(1);
        req.setName("Ana");
        req.setEmail("ana@example.com");
        req.setPasswordHash("hash");
        req.setIdCampaign(campaign);
        req.setStatus("PENDIENTE");
        return req;
    }

    @Test
    void getPendingRequests_delegates() {
        List<CaptainRequest> list = new ArrayList<>();
        when(captainRequestRepository.findByStatus("PENDIENTE")).thenReturn(list);
        assertSame(list, service.getPendingRequests());
    }

    @Test
    void approveRequest_createsUserAndCaptain() {
        CaptainRequest req = pendingRequest(5);
        when(captainRequestRepository.findById(1)).thenReturn(Optional.of(req));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            u.setIdUser(42);
            return u;
        });

        Integer newUserId = service.approveRequest(99, 1);

        assertEquals(Integer.valueOf(42), newUserId);
        assertEquals("APROBADA", req.getStatus());
        verify(captainRepository).save(any(Captain.class));
        verify(captainRequestRepository).save(req);
    }

    @Test
    void approveRequest_notFound_throws() {
        when(captainRequestRepository.findById(7)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.approveRequest(99, 7));
    }

    @Test
    void approveRequest_alreadyProcessed_throws() {
        CaptainRequest req = pendingRequest(5);
        req.setStatus("APROBADA");
        when(captainRequestRepository.findById(1)).thenReturn(Optional.of(req));
        assertThrows(IllegalStateException.class, () -> service.approveRequest(99, 1));
    }

    @Test
    void rejectRequest_marksRejected() {
        CaptainRequest req = pendingRequest(5);
        when(captainRequestRepository.findById(1)).thenReturn(Optional.of(req));

        service.rejectRequest(99, 1);

        assertEquals("RECHAZADA", req.getStatus());
        verify(captainRequestRepository).save(req);
    }

    @Test
    void rejectRequest_alreadyProcessed_throws() {
        CaptainRequest req = pendingRequest(5);
        req.setStatus("RECHAZADA");
        when(captainRequestRepository.findById(1)).thenReturn(Optional.of(req));
        assertThrows(IllegalStateException.class, () -> service.rejectRequest(99, 1));
    }
}
