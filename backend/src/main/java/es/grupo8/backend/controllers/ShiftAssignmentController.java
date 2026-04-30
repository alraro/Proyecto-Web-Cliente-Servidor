package es.grupo8.backend.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dto.AssignmentResponseDTO;
import es.grupo8.backend.dto.CaptainAssignmentRequestDTO;
import es.grupo8.backend.dto.ShiftAssignmentRequestDTO;
import es.grupo8.backend.entity.Volunteer;
import es.grupo8.backend.entity.VolunteerShift;
import es.grupo8.backend.security.CoordinatorGuard;
import es.grupo8.backend.services.ShiftAssignmentService;

@RestController
@RequestMapping("/api/shifts")
public class ShiftAssignmentController {

    @Autowired
    private ShiftAssignmentService shiftAssignmentService;

    @Autowired
    private CoordinatorGuard coordinatorGuard;

    /**
     * Assign volunteer to a shift
     * POST /api/shifts/assign-volunteer
     */
    @PostMapping("/assign-volunteer")
    public ResponseEntity<AssignmentResponseDTO> assignVolunteerToShift(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer campaignId,
            @RequestBody ShiftAssignmentRequestDTO request) {

        // Verify coordinator role
        if (!coordinatorGuard.isCoordinator(authHeader, campaignId)) {
            return ResponseEntity.status(403)
                    .body(new AssignmentResponseDTO(false, 
                            "Access denied: Coordinator role required for this operation", 
                            "ACCESS_DENIED"));
        }

        AssignmentResponseDTO response = shiftAssignmentService.assignVolunteerToShift(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Unassign volunteer from a shift
     * DELETE /api/shifts/unassign-volunteer?volunteerId={id}&campaignId={id}&storeId={id}&shiftDay={date}&startTime={time}
     */
    @DeleteMapping("/unassign-volunteer")
    public ResponseEntity<AssignmentResponseDTO> unassignVolunteerFromShift(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Integer volunteerId,
            @RequestParam Integer campaignId,
            @RequestParam Integer storeId,
            @RequestParam LocalDate shiftDay,
            @RequestParam LocalTime startTime,
            @RequestParam(required = false) Integer requestCampaignId) {

        // Verify coordinator role
        if (!coordinatorGuard.isCoordinator(authHeader, requestCampaignId != null ? requestCampaignId : campaignId)) {
            return ResponseEntity.status(403)
                    .body(new AssignmentResponseDTO(false, 
                            "Access denied: Coordinator role required for this operation", 
                            "ACCESS_DENIED"));
        }

        AssignmentResponseDTO response = shiftAssignmentService.unassignVolunteerFromShift(
                volunteerId, campaignId, storeId, shiftDay, startTime);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Assign captain to a campaign
     * POST /api/shifts/assign-captain
     */
    @PostMapping("/assign-captain")
    public ResponseEntity<AssignmentResponseDTO> assignCaptainToCampaign(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer campaignId,
            @RequestBody CaptainAssignmentRequestDTO request) {

        // Verify coordinator role
        if (!coordinatorGuard.isCoordinator(authHeader, campaignId)) {
            return ResponseEntity.status(403)
                    .body(new AssignmentResponseDTO(false, 
                            "Access denied: Coordinator role required for this operation", 
                            "ACCESS_DENIED"));
        }

        AssignmentResponseDTO response = shiftAssignmentService.assignCaptainToCampaign(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Unassign captain from a campaign
     * DELETE /api/shifts/unassign-captain?userId={id}&campaignId={id}
     */
    @DeleteMapping("/unassign-captain")
    public ResponseEntity<AssignmentResponseDTO> unassignCaptainFromCampaign(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Integer userId,
            @RequestParam Integer campaignId,
            @RequestParam(required = false) Integer requestCampaignId) {

        // Verify coordinator role
        if (!coordinatorGuard.isCoordinator(authHeader, requestCampaignId != null ? requestCampaignId : campaignId)) {
            return ResponseEntity.status(403)
                    .body(new AssignmentResponseDTO(false, 
                            "Access denied: Coordinator role required for this operation", 
                            "ACCESS_DENIED"));
        }

        AssignmentResponseDTO response = shiftAssignmentService.unassignCaptainFromCampaign(userId, campaignId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get all available volunteers
     * GET /api/shifts/volunteers
     */
    @GetMapping("/volunteers")
    public ResponseEntity<List<Volunteer>> getAvailableVolunteers(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer campaignId) {

        // Verify coordinator role
        if (!coordinatorGuard.isCoordinator(authHeader, campaignId)) {
            return ResponseEntity.status(403)
                    .body(null);
        }

        return ResponseEntity.ok(shiftAssignmentService.getAvailableVolunteers());
    }

    /**
     * Get shifts for a specific campaign, store, and day
     * GET /api/shifts?campaignId={id}&storeId={id}&shiftDay={date}
     */
    @GetMapping
    public ResponseEntity<List<VolunteerShift>> getShifts(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Integer campaignId,
            @RequestParam Integer storeId,
            @RequestParam LocalDate shiftDay,
            @RequestParam(required = false) Integer requestCampaignId) {

        // Verify coordinator role
        if (!coordinatorGuard.isCoordinator(authHeader, requestCampaignId != null ? requestCampaignId : campaignId)) {
            return ResponseEntity.status(403)
                    .body(null);
        }

        return ResponseEntity.ok(shiftAssignmentService.getShiftsForCampaignStoreDay(campaignId, storeId, shiftDay));
    }

    /**
     * Get current volunteer count for a shift
     * GET /api/shifts/count?campaignId={id}&storeId={id}&shiftDay={date}&startTime={time}
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getVolunteerCount(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Integer campaignId,
            @RequestParam Integer storeId,
            @RequestParam LocalDate shiftDay,
            @RequestParam LocalTime startTime,
            @RequestParam(required = false) Integer requestCampaignId) {

        // Verify coordinator role
        if (!coordinatorGuard.isCoordinator(authHeader, requestCampaignId != null ? requestCampaignId : campaignId)) {
            return ResponseEntity.status(403)
                    .body(null);
        }

        return ResponseEntity.ok(shiftAssignmentService.getCurrentVolunteerCount(campaignId, storeId, shiftDay, startTime));
    }
}