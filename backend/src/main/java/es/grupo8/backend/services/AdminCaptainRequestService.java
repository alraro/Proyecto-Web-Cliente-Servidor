/**
 * Servicio de gestión de solicitudes de alta de capitanes (admin).
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 85%
 * - IA Generativa: 15%
 */
package es.grupo8.backend.services;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CaptainRequestRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.entity.Captain;
import es.grupo8.backend.entity.CaptainId;
import es.grupo8.backend.entity.CaptainRequest;
import es.grupo8.backend.entity.UserEntity;
import lombok.AllArgsConstructor;

/**
 * Business logic for admin management of captain registration requests:
 * listing, approval (user + captain creation) and rejection.
 */
@Service
@AllArgsConstructor
public class AdminCaptainRequestService {

    private final CaptainRequestRepository captainRequestRepository;
    private final UserRepository userRepository;
    private final CaptainRepository captainRepository;

    /**
     * Returns the pending captain requests for the admin view.
     *
     * @return list of pending captain requests
     */
    @Transactional(readOnly = true)
    public List<CaptainRequest> getPendingRequests() {
        return captainRequestRepository.findByStatus("PENDIENTE");
    }

    /**
     * Returns captain requests filtered by status.
     *
     * @param status status filter (case-insensitive)
     * @return list of captain requests with the given status
     */
    @Transactional(readOnly = true)
    public List<CaptainRequest> getRequests(String status) {
        return captainRequestRepository.findByStatus(status.toUpperCase());
    }

    /**
     * Approves a captain request: creates the user account, assigns the captain role
     * to the request's campaign and marks the request as APROBADA. Atomic.
     *
     * @param adminUserId admin user identifier
     * @param requestId   captain request identifier
     * @return identifier of the newly created user
     * @throws NoSuchElementException if the request does not exist
     * @throws IllegalStateException  if the request is not pending
     */
    @Transactional
    public Integer approveRequest(Integer adminUserId, Integer requestId) {
        CaptainRequest req = captainRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));
        if (!"PENDIENTE".equals(req.getStatus())) {
            throw new IllegalStateException("Esta solicitud ya fue procesada");
        }

        UserEntity newUser = new UserEntity();
        newUser.setName(req.getName());
        newUser.setEmail(req.getEmail());
        newUser.setPassword(req.getPasswordHash());
        UserEntity savedUser = userRepository.save(newUser);

        CaptainId captainId = new CaptainId();
        captainId.setIdUser(savedUser.getIdUser());
        captainId.setIdCampaign(req.getIdCampaign().getId());

        Captain captain = new Captain();
        captain.setId(captainId);
        captain.setIdUser(savedUser);
        captain.setIdCampaign(req.getIdCampaign());
        captainRepository.save(captain);

        req.setStatus("APROBADA");
        req.setResolvedAt(Instant.now());
        captainRequestRepository.save(req);

        return savedUser.getIdUser();
    }

    /**
     * Rejects a captain request and marks it as RECHAZADA.
     *
     * @param adminUserId admin user identifier
     * @param requestId   captain request identifier
     * @throws NoSuchElementException if the request does not exist
     * @throws IllegalStateException  if the request is not pending
     */
    @Transactional
    public void rejectRequest(Integer adminUserId, Integer requestId) {
        CaptainRequest req = captainRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));
        if (!"PENDIENTE".equals(req.getStatus())) {
            throw new IllegalStateException("Esta solicitud ya fue procesada");
        }

        req.setStatus("RECHAZADA");
        req.setResolvedAt(Instant.now());
        captainRequestRepository.save(req);

    }
}
