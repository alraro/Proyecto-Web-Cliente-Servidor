package es.grupo8.backend.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
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
import org.mockito.InOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.CampaignTypeRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.CampaignType;
import es.grupo8.backend.security.AdminGuard;

class CampaignControllerTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    private CampaignController controller;
    private AdminGuard adminGuard;
    private CampaignRepository campaignRepository;
    private CampaignTypeRepository campaignTypeRepository;
    private CoordinatorRepository coordinatorRepository;
    private CaptainRepository captainRepository;
    private CampaignStoreRepository campaignStoreRepository;

    private CampaignType sampleTypeA;
    private CampaignType sampleTypeB;
    private Campaign sampleCampaign;
    private Campaign secondCampaign;

    @BeforeEach
    void setUp() {
        controller = new CampaignController();

        adminGuard = mock(AdminGuard.class);
        campaignRepository = mock(CampaignRepository.class);
        campaignTypeRepository = mock(CampaignTypeRepository.class);
        coordinatorRepository = mock(CoordinatorRepository.class);
        captainRepository = mock(CaptainRepository.class);
        campaignStoreRepository = mock(CampaignStoreRepository.class);

        ReflectionTestUtils.setField(controller, "adminGuard", adminGuard);
        ReflectionTestUtils.setField(controller, "campaignRepository", campaignRepository);
        ReflectionTestUtils.setField(controller, "campaignTypeRepository", campaignTypeRepository);
        ReflectionTestUtils.setField(controller, "coordinatorRepository", coordinatorRepository);
        ReflectionTestUtils.setField(controller, "captainRepository", captainRepository);
        ReflectionTestUtils.setField(controller, "campaignStoreRepository", campaignStoreRepository);

        sampleTypeA = new CampaignType();
        sampleTypeA.setId(2);
        sampleTypeA.setName("Gran Recogida");

        sampleTypeB = new CampaignType();
        sampleTypeB.setId(3);
        sampleTypeB.setName("Primavera");

        sampleCampaign = new Campaign();
        sampleCampaign.setId(1);
        sampleCampaign.setName("Gran Recogida Primavera 2025");
        sampleCampaign.setIdType(sampleTypeA);
        sampleCampaign.setStartDate(LocalDate.of(2025, 4, 1));
        sampleCampaign.setEndDate(LocalDate.of(2025, 4, 30));

        secondCampaign = new Campaign();
        secondCampaign.setId(2);
        secondCampaign.setName("Campana Otono 2025");
        secondCampaign.setIdType(sampleTypeB);
        secondCampaign.setStartDate(LocalDate.of(2025, 10, 1));
        secondCampaign.setEndDate(LocalDate.of(2025, 10, 31));

        // Required explicit getter navigation from the prompt.
        assertEquals(2, sampleCampaign.getIdType().getId());
    }

    @Test
    void createCampaign_whenNotAdmin_returns403() {
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(false);

        ResponseEntity<?> response = controller.createCampaign(AUTH_HEADER, validRequest());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void createCampaign_whenNameIsBlank_returns400() {
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);

        Map<String, Object> request = validRequest();
        request.put("name", "");

        ResponseEntity<?> response = controller.createCampaign(AUTH_HEADER, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(String.valueOf(body.get("message")).contains("Campaign name is required"));
    }

    @Test
    void createCampaign_whenEndDateBeforeStartDate_returns400() {
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);

        Map<String, Object> request = validRequest();
        request.put("startDate", "2025-10-31");
        request.put("endDate", "2025-10-01");

        ResponseEntity<?> response = controller.createCampaign(AUTH_HEADER, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(String.valueOf(body.get("message")).contains("End date must be after start date"));
    }

    @Test
    void createCampaign_whenTypeNotFound_returns404() {
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(campaignTypeRepository.findById(2)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.createCampaign(AUTH_HEADER, validRequest());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(String.valueOf(body.get("message")).contains("Campaign type not found"));
    }

    @Test
    void createCampaign_whenNameAlreadyExists_returns409() {
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(campaignTypeRepository.findById(2)).thenReturn(Optional.of(sampleTypeA));
        when(campaignRepository.existsByName("Gran Recogida Otono 2025")).thenReturn(true);

        ResponseEntity<?> response = controller.createCampaign(AUTH_HEADER, validRequest());

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(String.valueOf(body.get("message")).contains("already exists"));
    }

    @Test
    void createCampaign_happyPath_returns201AndSavesCampaign() {
        Logger auditLogger = (Logger) org.slf4j.LoggerFactory.getLogger("AUDIT");
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        auditLogger.addAppender(listAppender);

        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(adminGuard.extractUserId(AUTH_HEADER)).thenReturn(999);
        when(campaignTypeRepository.findById(2)).thenReturn(Optional.of(sampleTypeA));
        when(campaignRepository.existsByName("Gran Recogida Otono 2025")).thenReturn(false);
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> {
            Campaign toSave = invocation.getArgument(0);
            toSave.setId(5);
            return toSave;
        });

        ResponseEntity<?> response = controller.createCampaign(AUTH_HEADER, validRequest());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(campaignRepository, times(1)).save(any(Campaign.class));

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(body.containsKey("campaign"));

        @SuppressWarnings("unchecked")
        Map<String, Object> campaign = (Map<String, Object>) body.get("campaign");
        assertTrue(campaign.containsKey("id"));
        assertTrue(campaign.containsKey("name"));
        assertTrue(campaign.containsKey("type"));
        assertTrue(campaign.containsKey("startDate"));
        assertTrue(campaign.containsKey("endDate"));

        boolean actionLogged = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains("ACTION=CREATE_CAMPAIGN"));
        assertTrue(actionLogged);

        auditLogger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void updateCampaign_whenCampaignNotFound_returns404() {
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(campaignRepository.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.updateCampaign(AUTH_HEADER, 1, validRequest());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateCampaign_whenNameConflictWithOtherCampaign_returns409() {
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(campaignRepository.findById(1)).thenReturn(Optional.of(sampleCampaign));
        when(campaignTypeRepository.findById(2)).thenReturn(Optional.of(sampleTypeA));
        when(campaignRepository.existsByNameAndIdNot("Gran Recogida Otono 2025", 1)).thenReturn(true);

        ResponseEntity<?> response = controller.updateCampaign(AUTH_HEADER, 1, validRequest());

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void updateCampaign_happyPath_returns200AndSavesCampaign() {
        Logger auditLogger = (Logger) org.slf4j.LoggerFactory.getLogger("AUDIT");
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        auditLogger.addAppender(listAppender);

        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(adminGuard.extractUserId(AUTH_HEADER)).thenReturn(999);
        when(campaignRepository.findById(1)).thenReturn(Optional.of(sampleCampaign));
        when(campaignTypeRepository.findById(2)).thenReturn(Optional.of(sampleTypeA));
        when(campaignRepository.existsByNameAndIdNot("Gran Recogida Otono 2025", 1)).thenReturn(false);
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = controller.updateCampaign(AUTH_HEADER, 1, validRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(campaignRepository, times(1)).save(any(Campaign.class));

        boolean actionLogged = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains("ACTION=UPDATE_CAMPAIGN"));
        assertTrue(actionLogged);

        auditLogger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void deleteCampaign_whenNotAdmin_returns403() {
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(false);

        ResponseEntity<?> response = controller.deleteCampaign(AUTH_HEADER, 1);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void deleteCampaign_whenCampaignNotFound_returns404() {
        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(campaignRepository.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.deleteCampaign(AUTH_HEADER, 1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteCampaign_happyPath_returns200AndCascadeDeletes() {
        Logger auditLogger = (Logger) org.slf4j.LoggerFactory.getLogger("AUDIT");
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        auditLogger.addAppender(listAppender);

        when(adminGuard.isAdmin(AUTH_HEADER)).thenReturn(true);
        when(adminGuard.extractUserId(AUTH_HEADER)).thenReturn(999);
        when(campaignRepository.findById(1)).thenReturn(Optional.of(sampleCampaign));

        ResponseEntity<?> response = controller.deleteCampaign(AUTH_HEADER, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(coordinatorRepository).deleteAllByIdIdCampaign(1);
        verify(captainRepository).deleteAllByIdIdCampaign(1);
        verify(campaignStoreRepository).deleteByIdCampaign_Id(1);
        verify(campaignRepository).deleteById(1);

        InOrder inOrder = inOrder(coordinatorRepository, captainRepository, campaignStoreRepository, campaignRepository);
        inOrder.verify(coordinatorRepository).deleteAllByIdIdCampaign(1);
        inOrder.verify(captainRepository).deleteAllByIdIdCampaign(1);
        inOrder.verify(campaignStoreRepository).deleteByIdCampaign_Id(1);
        inOrder.verify(campaignRepository).deleteById(1);

        boolean actionLogged = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains("ACTION=DELETE_CAMPAIGN"));
        assertTrue(actionLogged);

        auditLogger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void listCampaigns_returns200WithCampaignList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Campaign> page = new PageImpl<>(List.of(sampleCampaign, secondCampaign), pageable, 2);

        when(campaignRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(campaignRepository.countByStartDateLessThanEqualAndEndDateGreaterThanEqual(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(1L);
        when(campaignRepository.countByEndDateBefore(any(LocalDate.class))).thenReturn(1L);
        when(campaignRepository.countByStartDateAfter(any(LocalDate.class))).thenReturn(1L);

        ResponseEntity<?> response = controller.getCampaigns(null, 0, 10, "startDate,desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) body.get("content");
        assertEquals(2, content.size());

        Map<String, Object> first = content.get(0);
        assertTrue(first.containsKey("id"));
        assertTrue(first.containsKey("name"));
        assertTrue(first.containsKey("type"));
        assertTrue(first.containsKey("startDate"));
        assertTrue(first.containsKey("endDate"));
        assertTrue(first.containsKey("status"));
    }

    @Test
    void getCampaign_whenNotFound_returns404() {
        when(campaignRepository.findById(99)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getCampaignById(99);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void listCampaignTypes_returns200WithTypeList() {
        when(campaignTypeRepository.findAll()).thenReturn(List.of(sampleTypeA, sampleTypeB));

        ResponseEntity<?> response = controller.getCampaignTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> body = (List<Map<String, Object>>) response.getBody();
        assertEquals(2, body.size());
    }

    private Map<String, Object> validRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Gran Recogida Otono 2025");
        request.put("typeId", 2);
        request.put("startDate", "2025-10-01");
        request.put("endDate", "2025-10-31");
        return request;
    }
}
