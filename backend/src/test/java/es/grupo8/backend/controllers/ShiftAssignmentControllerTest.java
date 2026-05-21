package es.grupo8.backend.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.ShiftCaptainRepository;
import es.grupo8.backend.dao.ShiftRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dao.VolunteerRepository;
import es.grupo8.backend.dao.VolunteerShiftRepository;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.CampaignStore;
import es.grupo8.backend.entity.CampaignStoreId;
import es.grupo8.backend.entity.Shift;
import es.grupo8.backend.entity.ShiftCaptain;
import es.grupo8.backend.entity.ShiftCaptainId;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.entity.Volunteer;
import es.grupo8.backend.entity.VolunteerShift;
import es.grupo8.backend.entity.VolunteerShiftId;

class ShiftAssignmentControllerTest {

    private static final String AUTH = "Bearer test-token";

    private ShiftAssignmentController controller;

    private ShiftRepository            shiftRepository;
    private VolunteerRepository        volunteerRepository;
    private VolunteerShiftRepository   volunteerShiftRepository;
    private ShiftCaptainRepository     shiftCaptainRepository;
    private UserRepository             userRepository;
    private CampaignStoreRepository    campaignStoreRepository;

    private Shift       sampleShift;
    private Volunteer   sampleVolunteer;
    private UserEntity  sampleCaptainUser;
    private CampaignStore sampleCampaignStore;

    @BeforeEach
    void setUp() {
        controller = new ShiftAssignmentController();

        shiftRepository          = mock(ShiftRepository.class);
        volunteerRepository      = mock(VolunteerRepository.class);
        volunteerShiftRepository = mock(VolunteerShiftRepository.class);
        shiftCaptainRepository   = mock(ShiftCaptainRepository.class);
        userRepository           = mock(UserRepository.class);
        campaignStoreRepository  = mock(CampaignStoreRepository.class);

        ReflectionTestUtils.setField(controller, "shiftRepository",          shiftRepository);
        ReflectionTestUtils.setField(controller, "volunteerRepository",       volunteerRepository);
        ReflectionTestUtils.setField(controller, "volunteerShiftRepository",  volunteerShiftRepository);
        ReflectionTestUtils.setField(controller, "shiftCaptainRepository",    shiftCaptainRepository);
        ReflectionTestUtils.setField(controller, "userRepository",            userRepository);
        ReflectionTestUtils.setField(controller, "campaignStoreRepository",   campaignStoreRepository);

        Campaign campaign = new Campaign();
        campaign.setId(1);
        campaign.setName("Campaña Test");
        campaign.setStartDate(LocalDate.of(2026, 5, 1));
        campaign.setEndDate(LocalDate.of(2026, 5, 31));

        Store store = new Store();
        store.setId(10);
        store.setName("Tienda Test");

        sampleShift = new Shift();
        sampleShift.setId(100);
        sampleShift.setIdCampaign(campaign);
        sampleShift.setIdStore(store);
        sampleShift.setShiftDay(LocalDate.of(2026, 5, 10));
        sampleShift.setStartTime(LocalTime.of(9, 0));
        sampleShift.setEndTime(LocalTime.of(12, 0));
        sampleShift.setVolunteersNeeded(3);
        sampleShift.setCreatedBy(1);

        sampleVolunteer = new Volunteer();
        sampleVolunteer.setId(20);
        sampleVolunteer.setName("Ana Voluntaria");
        sampleVolunteer.setEmail("ana@voluntaria.org");

        sampleCaptainUser = new UserEntity();
        sampleCaptainUser.setIdUser(30);
        sampleCaptainUser.setName("Luis Capitán");
        sampleCaptainUser.setEmail("luis@capitan.org");

        CampaignStoreId csId = new CampaignStoreId();
        csId.setIdCampaign(1);
        csId.setIdStore(10);
        sampleCampaignStore = new CampaignStore();
        sampleCampaignStore.setId(csId);
        sampleCampaignStore.setIdCampaign(campaign);
        sampleCampaignStore.setIdStore(store);
    }

    // ── Asignación de voluntarios ─────────────────────────────────────────────

    /**
     * Asignación exitosa: turno con capacidad libre, sin solapamiento.
     */
    @Test
    void assignVolunteer_success_returns201() {
        when(shiftRepository.findById(100)).thenReturn(Optional.of(sampleShift));
        when(volunteerRepository.findById(20)).thenReturn(Optional.of(sampleVolunteer));
        when(volunteerShiftRepository.existsById(any(VolunteerShiftId.class))).thenReturn(false);
        when(volunteerShiftRepository.countByShift(1, 10,
                LocalDate.of(2026, 5, 10), LocalTime.of(9, 0))).thenReturn(1L);
        when(volunteerShiftRepository.findOverlappingForVolunteer(
                20, LocalDate.of(2026, 5, 10), LocalTime.of(9, 0), LocalTime.of(12, 0)))
                .thenReturn(Collections.emptyList());
        when(campaignStoreRepository.findById(any(CampaignStoreId.class)))
                .thenReturn(Optional.of(sampleCampaignStore));
        when(volunteerShiftRepository.save(any(VolunteerShift.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.assignVolunteer(AUTH, 100, Map.of("volunteerId", 20));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(volunteerShiftRepository).save(any(VolunteerShift.class));
    }

    /**
     * RF-28: Aforo completo — no se puede asignar más voluntarios del máximo definido.
     */
    @Test
    void assignVolunteer_whenCapacityExceeded_returns400() {
        when(shiftRepository.findById(100)).thenReturn(Optional.of(sampleShift));
        when(volunteerRepository.findById(20)).thenReturn(Optional.of(sampleVolunteer));
        when(volunteerShiftRepository.existsById(any(VolunteerShiftId.class))).thenReturn(false);
        // volunteersNeeded = 3, ya hay 3 asignados
        when(volunteerShiftRepository.countByShift(1, 10,
                LocalDate.of(2026, 5, 10), LocalTime.of(9, 0))).thenReturn(3L);

        ResponseEntity<?> response = controller.assignVolunteer(AUTH, 100, Map.of("volunteerId", 20));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("CAPACITY_EXCEEDED", body.get("conflict"));
        verify(volunteerShiftRepository, never()).save(any());
    }

    /**
     * RF-28: Solapamiento de turno — el voluntario ya tiene otro turno en esa franja horaria.
     */
    @Test
    void assignVolunteer_whenOverlap_returns400() {
        when(shiftRepository.findById(100)).thenReturn(Optional.of(sampleShift));
        when(volunteerRepository.findById(20)).thenReturn(Optional.of(sampleVolunteer));
        when(volunteerShiftRepository.existsById(any(VolunteerShiftId.class))).thenReturn(false);
        when(volunteerShiftRepository.countByShift(1, 10,
                LocalDate.of(2026, 5, 10), LocalTime.of(9, 0))).thenReturn(0L);

        VolunteerShift overlapping = new VolunteerShift();
        VolunteerShiftId ovId = new VolunteerShiftId();
        ovId.setIdVolunteer(20);
        ovId.setIdCampaign(1);
        ovId.setIdStore(10);
        ovId.setShiftDay(LocalDate.of(2026, 5, 10));
        ovId.setStartTime(LocalTime.of(10, 0));
        overlapping.setId(ovId);
        overlapping.setEndTime(LocalTime.of(13, 0));
        when(volunteerShiftRepository.findOverlappingForVolunteer(
                20, LocalDate.of(2026, 5, 10), LocalTime.of(9, 0), LocalTime.of(12, 0)))
                .thenReturn(List.of(overlapping));

        ResponseEntity<?> response = controller.assignVolunteer(AUTH, 100, Map.of("volunteerId", 20));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("OVERLAP", body.get("conflict"));
        verify(volunteerShiftRepository, never()).save(any());
    }

    /**
     * Asignación duplicada de voluntario devuelve 409.
     */
    @Test
    void assignVolunteer_whenAlreadyAssigned_returns409() {
        when(shiftRepository.findById(100)).thenReturn(Optional.of(sampleShift));
        when(volunteerRepository.findById(20)).thenReturn(Optional.of(sampleVolunteer));
        when(volunteerShiftRepository.existsById(any(VolunteerShiftId.class))).thenReturn(true);

        ResponseEntity<?> response = controller.assignVolunteer(AUTH, 100, Map.of("volunteerId", 20));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(volunteerShiftRepository, never()).save(any());
    }

    /**
     * Turno inexistente devuelve 404.
     */
    @Test
    void assignVolunteer_whenShiftNotFound_returns404() {
        when(shiftRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.assignVolunteer(AUTH, 999, Map.of("volunteerId", 20));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Voluntario inexistente devuelve 404.
     */
    @Test
    void assignVolunteer_whenVolunteerNotFound_returns404() {
        when(shiftRepository.findById(100)).thenReturn(Optional.of(sampleShift));
        when(volunteerRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.assignVolunteer(AUTH, 100, Map.of("volunteerId", 999));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── Asignación de capitanes ───────────────────────────────────────────────

    /**
     * Asignación exitosa de capitán sin solapamiento.
     */
    @Test
    void assignCaptain_success_returns201() {
        when(shiftRepository.findById(100)).thenReturn(Optional.of(sampleShift));
        when(userRepository.findById(30)).thenReturn(Optional.of(sampleCaptainUser));
        when(shiftCaptainRepository.existsById(any(ShiftCaptainId.class))).thenReturn(false);
        when(shiftCaptainRepository.findOverlappingForCaptain(
                30, LocalDate.of(2026, 5, 10), LocalTime.of(9, 0), LocalTime.of(12, 0), 100))
                .thenReturn(Collections.emptyList());
        when(shiftCaptainRepository.save(any(ShiftCaptain.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.assignCaptain(AUTH, 100, Map.of("userId", 30));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(shiftCaptainRepository).save(any(ShiftCaptain.class));
    }

    /**
     * RF-28: Solapamiento para capitán devuelve 400.
     */
    @Test
    void assignCaptain_whenOverlap_returns400() {
        when(shiftRepository.findById(100)).thenReturn(Optional.of(sampleShift));
        when(userRepository.findById(30)).thenReturn(Optional.of(sampleCaptainUser));
        when(shiftCaptainRepository.existsById(any(ShiftCaptainId.class))).thenReturn(false);

        Shift overlappingShift = new Shift();
        overlappingShift.setId(200);
        overlappingShift.setStartTime(LocalTime.of(10, 0));
        overlappingShift.setEndTime(LocalTime.of(13, 0));
        ShiftCaptain overlapping = new ShiftCaptain();
        ShiftCaptainId ovId = new ShiftCaptainId();
        ovId.setIdShift(200);
        ovId.setIdUser(30);
        overlapping.setId(ovId);
        overlapping.setShift(overlappingShift);
        overlapping.setUser(sampleCaptainUser);
        when(shiftCaptainRepository.findOverlappingForCaptain(
                30, LocalDate.of(2026, 5, 10), LocalTime.of(9, 0), LocalTime.of(12, 0), 100))
                .thenReturn(List.of(overlapping));

        ResponseEntity<?> response = controller.assignCaptain(AUTH, 100, Map.of("userId", 30));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("OVERLAP", body.get("conflict"));
        verify(shiftCaptainRepository, never()).save(any());
    }

    /**
     * Asignación duplicada de capitán devuelve 409.
     */
    @Test
    void assignCaptain_whenAlreadyAssigned_returns409() {
        when(shiftRepository.findById(100)).thenReturn(Optional.of(sampleShift));
        when(userRepository.findById(30)).thenReturn(Optional.of(sampleCaptainUser));
        when(shiftCaptainRepository.existsById(any(ShiftCaptainId.class))).thenReturn(true);

        ResponseEntity<?> response = controller.assignCaptain(AUTH, 100, Map.of("userId", 30));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(shiftCaptainRepository, never()).save(any());
    }

    /**
     * Turno inexistente devuelve 404 para asignación de capitán.
     */
    @Test
    void assignCaptain_whenShiftNotFound_returns404() {
        when(shiftRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.assignCaptain(AUTH, 999, Map.of("userId", 30));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Usuario inexistente devuelve 404 para asignación de capitán.
     */
    @Test
    void assignCaptain_whenUserNotFound_returns404() {
        when(shiftRepository.findById(100)).thenReturn(Optional.of(sampleShift));
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.assignCaptain(AUTH, 100, Map.of("userId", 999));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
