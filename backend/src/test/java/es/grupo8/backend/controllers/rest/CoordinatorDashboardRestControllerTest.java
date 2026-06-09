package es.grupo8.backend.controllers.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.CoordinatorDashboardService;
import es.grupo8.backend.services.UserService;

/**
 * Unit tests for {@link CoordinatorDashboardRestController}: coordinator auth enforcement
 * and null-body handling.
 * Uses direct method invocation — no Spring context or HTTP layer required.
 */
@ExtendWith(MockitoExtension.class)
class CoordinatorDashboardRestControllerTest {

    @Mock UserService userService;
    @Mock AuthService authService;
    @Mock CoordinatorDashboardService coordinatorDashboardService;

    @InjectMocks CoordinatorDashboardRestController controller;

    /** getMyCampaigns with a non-coordinator token must return 403. */
    @Test
    void getMyCampaigns_notCoordinator_returnsForbidden() {
        when(userService.isCoordinatorFromToken("Bearer bad-token")).thenReturn(false);

        ResponseEntity<?> response = controller.getMyCampaigns("Bearer bad-token");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    /** getMyCampaigns with a valid coordinator token must return 200. */
    @Test
    void getMyCampaigns_validToken_returnsOk() {
        when(userService.isCoordinatorFromToken("Bearer test-token")).thenReturn(true);
        when(authService.extractUserIdFromToken("Bearer test-token")).thenReturn(1);
        when(coordinatorDashboardService.getMyCampaigns(1)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = controller.getMyCampaigns("Bearer test-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /** createVolunteer with a null body and valid token must return 400. */
    @Test
    void createVolunteer_missingBody_returns400() {
        when(userService.isCoordinatorFromToken("Bearer test-token")).thenReturn(true);

        ResponseEntity<?> response = controller.createVolunteer("Bearer test-token", null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /** assignVolunteerShift with a non-coordinator token must return 403. */
    @Test
    void assignVolunteerShift_notCoordinator_returnsForbidden() {
        when(userService.isCoordinatorFromToken("Bearer bad-token")).thenReturn(false);

        ResponseEntity<?> response = controller.assignVolunteerShift("Bearer bad-token", null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
