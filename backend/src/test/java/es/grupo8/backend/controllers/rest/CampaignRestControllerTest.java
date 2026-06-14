package es.grupo8.backend.controllers.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.dto.CampaignRequestDto;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.CampaignService;
import es.grupo8.backend.services.UserService;

@ExtendWith(MockitoExtension.class)
class CampaignRestControllerTest {

    @Mock UserService userService;
    @Mock AuthService authService;
    @Mock CampaignService campaignService;

    @InjectMocks CampaignRestController controller;

    @Test
    void getCampaigns_returnsOk() {
        when(campaignService.getCampaigns(any(), any(Pageable.class))).thenReturn(Page.empty());
        when(campaignService.getCampaignCounts()).thenReturn(new long[]{0L, 0L, 0L});

        ResponseEntity<?> response = controller.getCampaigns(null, 0, 10, "startDate,desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createCampaign_notAdmin_returnsForbidden() {
        when(userService.isAdminFromToken("Bearer bad-token")).thenReturn(false);

        ResponseEntity<?> response = controller.createCampaign("Bearer bad-token", new CampaignRequestDto());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void createCampaign_noToken_returnsForbidden() {
        when(userService.isAdminFromToken(null)).thenReturn(false);

        ResponseEntity<?> response = controller.createCampaign(null, new CampaignRequestDto());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void createCampaign_admin_returnsCreated() {
        CampaignRequestDto req = new CampaignRequestDto();
        req.setName("Test Campaign");
        req.setTypeId(1);
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusDays(30));

        CampaignDTO created = new CampaignDTO();
        created.setId(1);
        created.setName("Test Campaign");

        when(userService.isAdminFromToken("Bearer test-token")).thenReturn(true);
        when(authService.extractUserIdFromToken("Bearer test-token")).thenReturn(1);
        when(campaignService.createCampaign(eq(1), any())).thenReturn(created);

        ResponseEntity<?> response = controller.createCampaign("Bearer test-token", req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Campaign created successfully", body.get("message"));
    }

    @Test
    void handleNotFound_returns404() {
        ResponseEntity<Map<String, String>> response =
                controller.handleNotFound(new NoSuchElementException("Campaign not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Campaign not found",
                Objects.requireNonNull(response.getBody()).get("message"));
    }

    @Test
    void deleteCampaign_notAdmin_returnsForbidden() {
        when(userService.isAdminFromToken(any())).thenReturn(false);

        ResponseEntity<?> response = controller.deleteCampaign("Bearer bad-token", 1);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
