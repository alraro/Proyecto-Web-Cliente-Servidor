/**
 * Pruebas unitarias del controlador REST del panel del coordinador.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 85%
 * - IA Generativa: 15%
 */
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

@ExtendWith(MockitoExtension.class)
class CoordinatorDashboardRestControllerTest {

    @Mock UserService userService;
    @Mock AuthService authService;
    @Mock CoordinatorDashboardService coordinatorDashboardService;

    @InjectMocks CoordinatorDashboardRestController controller;

    @Test
    void getMyCampaigns_notCoordinator_returnsForbidden() {
        when(userService.isCoordinatorFromToken("Bearer bad-token")).thenReturn(false);

        ResponseEntity<?> response = controller.getMyCampaigns("Bearer bad-token");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getMyCampaigns_validToken_returnsOk() {
        when(userService.isCoordinatorFromToken("Bearer test-token")).thenReturn(true);
        when(authService.extractUserIdFromToken("Bearer test-token")).thenReturn(1);
        when(coordinatorDashboardService.getMyCampaigns(1)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = controller.getMyCampaigns("Bearer test-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createVolunteer_missingBody_returns400() {
        when(userService.isCoordinatorFromToken("Bearer test-token")).thenReturn(true);

        ResponseEntity<?> response = controller.createVolunteer("Bearer test-token", null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void assignVolunteerShift_notCoordinator_returnsForbidden() {
        when(userService.isCoordinatorFromToken("Bearer bad-token")).thenReturn(false);

        ResponseEntity<?> response = controller.assignVolunteerShift("Bearer bad-token", null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
