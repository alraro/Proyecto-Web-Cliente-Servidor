package es.grupo8.backend.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import es.grupo8.backend.services.CoordinatorDashboardService;
import es.grupo8.backend.services.UserService;

/**
 * Unit tests for {@link CoordinatorController}: session-based auth redirect and
 * model population for the campaigns page.
 * Uses direct method invocation — no Spring context or HTTP layer required.
 */
@ExtendWith(MockitoExtension.class)
class CoordinatorControllerTest {

    @Mock UserService userService;
    @Mock CoordinatorDashboardService coordinatorDashboardService;

    @InjectMocks CoordinatorController controller;

    /**
     * coordinatorCampaigns with a session token that fails the coordinator check
     * must return the redirect view string.
     */
    @Test
    void coordinatorCampaigns_notCoordinator_redirectsToLogin() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("token", "bad-token");
        when(userService.isCoordinatorFromToken("Bearer bad-token")).thenReturn(false);

        String view = controller.coordinatorCampaigns(session, new ExtendedModelMap());

        assertEquals("redirect:/login", view);
    }

    /**
     * coordinatorCampaigns with a valid session returns the correct view name
     * and adds the campaigns list to the model.
     */
    @Test
    void coordinatorCampaigns_validSession_returnsViewWithModel() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("token", "test-token");
        session.setAttribute("userID", 1);
        when(userService.isCoordinatorFromToken("Bearer test-token")).thenReturn(true);
        when(coordinatorDashboardService.getMyCampaigns(eq(1))).thenReturn(Collections.emptyList());

        Model model = new ExtendedModelMap();
        String view = controller.coordinatorCampaigns(session, model);

        assertEquals("coordinator-campaigns", view);
        assertTrue(model.containsAttribute("campaigns"));
    }
}
