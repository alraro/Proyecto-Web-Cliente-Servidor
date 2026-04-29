package es.grupo8.backend.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.Captain;
import es.grupo8.backend.entity.CaptainId;
import es.grupo8.backend.entity.Coordinator;
import es.grupo8.backend.entity.CoordinatorId;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.security.AdminGuard;

class CampaignAssignmentControllerTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    private CampaignAssignmentController controller;
    private AdminGuard adminGuard;
    private CampaignRepository campaignRepository;
    private UserRepository userRepository;
    private CoordinatorRepository coordinatorRepository;
    private CaptainRepository captainRepository;

    private Campaign sampleCampaign;
    private UserEntity sampleCoordinatorUser;
    private UserEntity sampleCaptainUser;

    @BeforeEach
    void setUp() {
        controller = new CampaignAssignmentController();

        adminGuard = mock(AdminGuard.class);
        campaignRepository = mock(CampaignRepository.class);
        userRepository = mock(UserRepository.class);
        coordinatorRepository = mock(CoordinatorRepository.class);
        captainRepository = mock(CaptainRepository.class);

        ReflectionTestUtils.setField(controller, "adminGuard", adminGuard);
        ReflectionTestUtils.setField(controller, "campaignRepository", campaignRepository);
        ReflectionTestUtils.setField(controller, "userRepository", userRepository);
        ReflectionTestUtils.setField(controller, "coordinatorRepository", coordinatorRepository);
        ReflectionTestUtils.setField(controller, "captainRepository", captainRepository);

        // Reusable campaign fixture with all fields required by response mapping.
        sampleCampaign = new Campaign();
        sampleCampaign.setId(1);
        sampleCampaign.setName("Gran Recogida Primavera 2025");
        sampleCampaign.setStartDate(LocalDate.of(2025, 4, 1));
        sampleCampaign.setEndDate(LocalDate.of(2025, 4, 30));

        // Reusable coordinator fixture used in assignment and listing tests.
        sampleCoordinatorUser = new UserEntity();
        sampleCoordinatorUser.setIdUser(3);
        sampleCoordinatorUser.setName("Ana Garcia");
        sampleCoordinatorUser.setEmail("ana@bancosol.org");
        sampleCoordinatorUser.setPhone("600000001");
        sampleCoordinatorUser.setAddress("Calle A");
        sampleCoordinatorUser.setPostalCode("29001");
        sampleCoordinatorUser.setPassword("hashed-password");

        // Reusable captain fixture used in assignment and listing tests.
        sampleCaptainUser = new UserEntity();
        sampleCaptainUser.setIdUser(7);
        sampleCaptainUser.setName("Luis Perez");
        sampleCaptainUser.setEmail("luis@bancosol.org");
        sampleCaptainUser.setPhone("600000002");
        sampleCaptainUser.setAddress("Calle B");
        sampleCaptainUser.setPostalCode("29002");
        sampleCaptainUser.setPassword("hashed-password");
    }

    /**
     * Verifies that listing campaigns is forbidden when the authenticated user is not an administrator.
     */
    @Test
    void getCampaigns_whenNotAdmin_returns403() {
        // Simulate a non-admin caller.
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(false);

        ResponseEntity<?> response = controller.getCampaigns(AUTH_HEADER);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    /**
     * Verifies that assigning a coordinator is forbidden when the authenticated user is not an administrator.
     */
    @Test
    void assignCoordinator_whenNotAdmin_returns403() {
        // Simulate a non-admin caller.
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(false);

        ResponseEntity<?> response = controller.assignCoordinator(AUTH_HEADER, 1, requestWithUserId(3));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    /**
     * Verifies that assigning a coordinator fails with 404 when the campaign does not exist.
     */
    @Test
    void assignCoordinator_whenCampaignNotFound_returns404() {
        // Admin check passes, but the campaign lookup returns empty.
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(campaignRepository.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.assignCoordinator(AUTH_HEADER, 1, requestWithUserId(3));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Verifies that assigning a coordinator fails with 404 when the user does not exist.
     */
    @Test
    void assignCoordinator_whenUserNotFound_returns404() {
        // Campaign exists, but the selected user does not.
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(campaignRepository.findById(1)).thenReturn(Optional.of(sampleCampaign));
        when(userRepository.findById(3)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.assignCoordinator(AUTH_HEADER, 1, requestWithUserId(3));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Verifies that assigning a coordinator fails with 400 when the user does not hold the coordinator role.
     */
    @Test
    void assignCoordinator_whenUserIsNotCoordinator_returns400() {
        // Campaign and user exist, but role validation fails.
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(campaignRepository.findById(1)).thenReturn(Optional.of(sampleCampaign));
        when(userRepository.findById(3)).thenReturn(Optional.of(sampleCoordinatorUser));
        when(userRepository.isCoordinator(3)).thenReturn(false);

        ResponseEntity<?> response = controller.assignCoordinator(AUTH_HEADER, 1, requestWithUserId(3));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(String.valueOf(body.get("message")).contains("User does not have the coordinator role"));
    }

    /**
     * Verifies that assigning a coordinator fails with 409 when the user is already assigned in the campaign.
     */
    @Test
    void assignCoordinator_whenAlreadyAssigned_returns409() {
        // All validations pass except duplicate assignment check.
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(campaignRepository.findById(1)).thenReturn(Optional.of(sampleCampaign));
        when(userRepository.findById(3)).thenReturn(Optional.of(sampleCoordinatorUser));
        when(userRepository.isCoordinator(3)).thenReturn(true);
        when(coordinatorRepository.existsByIdIdUserAndIdIdCampaign(3, 1)).thenReturn(true);

        ResponseEntity<?> response = controller.assignCoordinator(AUTH_HEADER, 1, requestWithUserId(3));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    /**
     * Verifies that assigning a coordinator succeeds with 201, persists the entity, and emits the audit action.
     */
    @Test
    void assignCoordinator_happyPath_returns201AndPersistsEntity() {
        // Prepare a log appender to assert the audit action emitted by the controller.
        ch.qos.logback.classic.Logger auditLogger =
                (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("AUDIT");
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        auditLogger.addAppender(listAppender);

        // Configure the happy path where all validations pass.
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(adminGuard.extractUserId(AUTH_HEADER)).thenReturn(999);
        when(campaignRepository.findById(1)).thenReturn(Optional.of(sampleCampaign));
        when(userRepository.findById(3)).thenReturn(Optional.of(sampleCoordinatorUser));
        when(userRepository.isCoordinator(3)).thenReturn(true);
        when(coordinatorRepository.existsByIdIdUserAndIdIdCampaign(3, 1)).thenReturn(false);

        ResponseEntity<?> response = controller.assignCoordinator(AUTH_HEADER, 1, requestWithUserId(3));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(coordinatorRepository, times(1)).save(any(Coordinator.class));

        // Validate the exact action token in the audit log line.
        boolean actionLogged = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains("ACTION=ASSIGN_COORDINATOR"));
        assertTrue(actionLogged);

        auditLogger.detachAppender(listAppender);
        listAppender.stop();
    }

    /**
     * Verifies that unassigning a coordinator fails with 404 when the assignment is missing.
     */
    @Test
    void unassignCoordinator_whenAssignmentNotFound_returns404() {
        // Campaign exists, but assignment lookup fails.
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(campaignRepository.existsById(1)).thenReturn(true);
        when(coordinatorRepository.existsByIdIdUserAndIdIdCampaign(3, 1)).thenReturn(false);

        ResponseEntity<?> response = controller.unassignCoordinator(AUTH_HEADER, 1, 3);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Verifies that unassigning a coordinator succeeds with 200 and triggers a delete operation.
     */
    @Test
    void unassignCoordinator_happyPath_returns200AndDeletesEntity() {
        // Campaign and assignment exist.
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(campaignRepository.existsById(1)).thenReturn(true);
        when(coordinatorRepository.existsByIdIdUserAndIdIdCampaign(3, 1)).thenReturn(true);

        ResponseEntity<?> response = controller.unassignCoordinator(AUTH_HEADER, 1, 3);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(coordinatorRepository, times(1)).deleteByIdIdUserAndIdIdCampaign(3, 1);
    }

    /**
     * Verifies that assigning a captain fails with 400 when the user does not hold the captain role.
     */
    @Test
    void assignCaptain_whenUserIsNotCaptain_returns400() {
        // Campaign and user exist, but captain role validation fails.
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(campaignRepository.findById(1)).thenReturn(Optional.of(sampleCampaign));
        when(userRepository.findById(7)).thenReturn(Optional.of(sampleCaptainUser));
        when(userRepository.isCaptain(7)).thenReturn(false);

        ResponseEntity<?> response = controller.assignCaptain(AUTH_HEADER, 1, requestWithUserId(7));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(String.valueOf(body.get("message")).contains("User does not have the captain role"));
    }

    /**
     * Verifies that assignments retrieval returns both coordinator and captain lists with expected user fields.
     */
    @Test
    void getCampaignAssignments_happyPath_returnsCoordinatorsAndCaptains() {
        // Build one coordinator assignment and one captain assignment for the same campaign.
        CoordinatorId coordinatorId = new CoordinatorId();
        coordinatorId.setIdUser(sampleCoordinatorUser.getIdUser());
        coordinatorId.setIdCampaign(sampleCampaign.getId());
        Coordinator coordinator = new Coordinator();
        coordinator.setId(coordinatorId);

        CaptainId captainId = new CaptainId();
        captainId.setIdUser(sampleCaptainUser.getIdUser());
        captainId.setIdCampaign(sampleCampaign.getId());
        Captain captain = new Captain();
        captain.setId(captainId);

        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(campaignRepository.findById(1)).thenReturn(Optional.of(sampleCampaign));
        when(coordinatorRepository.findByIdIdCampaign(1)).thenReturn(List.of(coordinator));
        when(captainRepository.findByIdIdCampaign(1)).thenReturn(List.of(captain));
        when(userRepository.findById(sampleCoordinatorUser.getIdUser())).thenReturn(Optional.of(sampleCoordinatorUser));
        when(userRepository.findById(sampleCaptainUser.getIdUser())).thenReturn(Optional.of(sampleCaptainUser));

        ResponseEntity<?> response = controller.getCampaignAssignments(AUTH_HEADER, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(body.containsKey("coordinators"));
        assertTrue(body.containsKey("captains"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> coordinators = (List<Map<String, Object>>) body.get("coordinators");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> captains = (List<Map<String, Object>>) body.get("captains");

        assertEquals(1, coordinators.size());
        assertEquals(1, captains.size());

        Map<String, Object> coordinatorItem = coordinators.get(0);
        assertTrue(coordinatorItem.containsKey("userId"));
        assertTrue(coordinatorItem.containsKey("name"));
        assertTrue(coordinatorItem.containsKey("email"));

        Map<String, Object> captainItem = captains.get(0);
        assertTrue(captainItem.containsKey("userId"));
        assertTrue(captainItem.containsKey("name"));
        assertTrue(captainItem.containsKey("email"));
    }

    /**
     * Verifies that available-users endpoint rejects unknown role values with 400.
     */
    @Test
    void getAvailableUsers_whenInvalidRole_returns400() {
        // Keep campaign valid so the role validation branch is reached.
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(campaignRepository.findById(1)).thenReturn(Optional.of(sampleCampaign));

        ResponseEntity<?> response = controller.getAvailableUsers(AUTH_HEADER, 1, "UNKNOWN");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(String.valueOf(body.get("message")).contains("Invalid role. Use COORDINATOR or CAPTAIN"));
    }

    private Map<String, Object> requestWithUserId(Object userId) {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        return request;
    }
}
