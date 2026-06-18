package es.grupo8.backend.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

@ExtendWith(MockitoExtension.class)
class CoordinatorControllerTest {

    @Mock CoordinatorDashboardService coordinatorDashboardService;

    @InjectMocks CoordinatorController controller;

    @Test
    void coordinatorCampaigns_notCoordinator_redirectsToLogin() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "CAPITAN");

        String view = controller.coordinatorCampaigns(session, new ExtendedModelMap());

        assertEquals("redirect:/login", view);
    }

    @Test
    void coordinatorCampaigns_validSession_returnsViewWithModel() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "COORDINADOR");
        session.setAttribute("userID", 1);
        when(coordinatorDashboardService.getMyCampaigns(1)).thenReturn(Collections.emptyList());

        Model model = new ExtendedModelMap();
        String view = controller.coordinatorCampaigns(session, model);

        assertEquals("coordinator-campaigns", view);
        assertTrue(model.containsAttribute("campaigns"));
    }
}
