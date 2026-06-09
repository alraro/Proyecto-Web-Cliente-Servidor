package es.grupo8.backend.controllers.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.CaptainDashboardService;
import es.grupo8.backend.services.UserService;

/**
 * Unit tests for {@link CaptainDashboardRestController}: captain auth enforcement,
 * null-body handling, and incident creation.
 * Uses direct method invocation — no Spring context or HTTP layer required.
 */
@ExtendWith(MockitoExtension.class)
class CaptainDashboardRestControllerTest {

    @Mock UserService userService;
    @Mock AuthService authService;
    @Mock CaptainDashboardService captainDashboardService;

    @InjectMocks CaptainDashboardRestController controller;

    /** getMyCampaigns with a non-captain token must return 403. */
    @Test
    void getMyCampaigns_notCaptain_returnsForbidden() {
        when(userService.isCaptainFromToken("Bearer bad-token")).thenReturn(false);

        ResponseEntity<?> response = controller.getMyCampaigns("Bearer bad-token");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    /** getShifts with a valid captain token must return 200. */
    @Test
    void getShifts_validCaptain_returnsOk() {
        when(userService.isCaptainFromToken("Bearer test-token")).thenReturn(true);
        when(authService.extractUserIdFromToken("Bearer test-token")).thenReturn(1);
        when(captainDashboardService.getShifts(any(), any(), any())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = controller.getShifts("Bearer test-token", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /** createIncident with a null body and valid token must return 400. */
    @Test
    void createIncident_missingBody_returns400() {
        when(userService.isCaptainFromToken("Bearer test-token")).thenReturn(true);

        ResponseEntity<?> response = controller.createIncident("Bearer test-token", null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /** createIncident with valid token and full body must return 201 with incidentId. */
    @Test
    void createIncident_validRequest_returns201() {
        when(userService.isCaptainFromToken("Bearer test-token")).thenReturn(true);
        when(authService.extractUserIdFromToken("Bearer test-token")).thenReturn(1);
        when(captainDashboardService.createIncident(any(), any(), any(), any())).thenReturn(42);

        Map<String, Object> body = new HashMap<>();
        body.put("campaignId", 1);
        body.put("storeId", 2);
        body.put("description", "Test incident");

        ResponseEntity<?> response = controller.createIncident("Bearer test-token", body);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(42, responseBody.get("incidentId"));
    }
}
