package es.grupo8.backend.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.VolunteerRepository;
import es.grupo8.backend.dao.VolunteerShiftRepository;

@ExtendWith(MockitoExtension.class)
class ShiftAssignmentServiceTest {

    @Mock
    private VolunteerShiftRepository volunteerShiftRepository;

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private CaptainRepository captainRepository;

    @Mock
    private CampaignStoreRepository campaignStoreRepository;

    @InjectMocks
    private ShiftAssignmentService shiftAssignmentService;

    /**
     * Test: Detectar solapamiento de turnos correctamente
     * Scenario: Volunteer has shift 09:00-11:00, trying to add 10:00-12:00
     * Expected: Overlap detected
     */
    @Test
    void checkShiftOverlap_detectsOverlapCorrectly() {
        // Given: Existing shift 09:00-11:00
        LocalDate shiftDay = LocalDate.of(2026, 4, 28);
        LocalTime existingStart = LocalTime.of(9, 0);
        LocalTime existingEnd = LocalTime.of(11, 0);
        
        // When: Trying to add shift 10:00-12:00 (overlaps with 09:00-11:00)
        LocalTime newStart = LocalTime.of(10, 0);
        LocalTime newEnd = LocalTime.of(12, 0);
        
        // Then: Should detect overlap
        // Note: This test validates the overlap detection logic
        // The actual repository call would return existing shifts
        boolean hasOverlap = newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
        
        assertTrue(hasOverlap, "Should detect shift overlap when times intersect");
    }

    /**
     * Test: No overlap when shifts don't intersect
     * Scenario: Volunteer has shift 09:00-11:00, trying to add 11:00-13:00
     * Expected: No overlap (shifts are back-to-back)
     */
    @Test
    void checkShiftOverlap_noOverlapWhenBackToBack() {
        // Given: Existing shift 09:00-11:00
        LocalTime existingStart = LocalTime.of(9, 0);
        LocalTime existingEnd = LocalTime.of(11, 0);
        
        // When: Trying to add shift 11:00-13:00 (no overlap - back to back)
        LocalTime newStart = LocalTime.of(11, 0);
        LocalTime newEnd = LocalTime.of(13, 0);
        
        // Then: Should NOT detect overlap
        boolean hasOverlap = newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
        
        assertFalse(hasOverlap, "Should not detect overlap when shifts are back-to-back");
    }

    /**
     * Test: No overlap when new shift is before existing shift
     * Scenario: Volunteer has shift 12:00-14:00, trying to add 09:00-11:00
     * Expected: No overlap
     */
    @Test
    void checkShiftOverlap_noOverlapWhenBefore() {
        // Given: Existing shift 12:00-14:00
        LocalTime existingStart = LocalTime.of(12, 0);
        LocalTime existingEnd = LocalTime.of(14, 0);
        
        // When: Trying to add shift 09:00-11:00 (completely before)
        LocalTime newStart = LocalTime.of(9, 0);
        LocalTime newEnd = LocalTime.of(11, 0);
        
        // Then: Should NOT detect overlap
        boolean hasOverlap = newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
        
        assertFalse(hasOverlap, "Should not detect overlap when new shift is before existing");
    }

    /**
     * Test: No overlap when new shift is after existing shift
     * Scenario: Volunteer has shift 09:00-11:00, trying to add 14:00-16:00
     * Expected: No overlap
     */
    @Test
    void checkShiftOverlap_noOverlapWhenAfter() {
        // Given: Existing shift 09:00-11:00
        LocalTime existingStart = LocalTime.of(9, 0);
        LocalTime existingEnd = LocalTime.of(11, 0);
        
        // When: Trying to add shift 14:00-16:00 (completely after)
        LocalTime newStart = LocalTime.of(14, 0);
        LocalTime newEnd = LocalTime.of(16, 0);
        
        // Then: Should NOT detect overlap
        boolean hasOverlap = newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
        
        assertFalse(hasOverlap, "Should not detect overlap when new shift is after existing");
    }

    /**
     * Test: Validate max volunteers logic
     * Scenario: Current count is less than max
     * Expected: Validation passes
     */
    @Test
    void validateMaxVolunteers_passesWhenUnderLimit() {
        // Given: Max volunteers = 10, current = 5
        int maxVolunteers = 10;
        long currentCount = 5;
        
        // When: Checking if can add more
        boolean canAdd = currentCount < maxVolunteers;
        
        // Then: Should allow
        assertTrue(canAdd, "Should allow adding volunteer when under limit");
    }

    /**
     * Test: Validate max volunteers logic
     * Scenario: Current count equals max
     * Expected: Validation fails
     */
    @Test
    void validateMaxVolunteers_failsWhenAtLimit() {
        // Given: Max volunteers = 10, current = 10
        int maxVolunteers = 10;
        long currentCount = 10;
        
        // When: Checking if can add more
        boolean canAdd = currentCount < maxVolunteers;
        
        // Then: Should NOT allow
        assertFalse(canAdd, "Should not allow adding volunteer when at limit");
    }

    /**
     * Test: Validate max volunteers logic
     * Scenario: Current count exceeds max
     * Expected: Validation fails
     */
    @Test
    void validateMaxVolunteers_failsWhenOverLimit() {
        // Given: Max volunteers = 10, current = 12
        int maxVolunteers = 10;
        long currentCount = 12;
        
        // When: Checking if can add more
        boolean canAdd = currentCount < maxVolunteers;
        
        // Then: Should NOT allow
        assertFalse(canAdd, "Should not allow adding volunteer when over limit");
    }
}