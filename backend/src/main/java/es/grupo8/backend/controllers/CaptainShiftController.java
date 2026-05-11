package es.grupo8.backend.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dao.ShiftCaptainRepository;
import es.grupo8.backend.dao.VolunteerShiftRepository;
import es.grupo8.backend.entity.Shift;
import es.grupo8.backend.entity.VolunteerShift;
import es.grupo8.backend.security.CaptainGuard;

@RestController
@RequestMapping("/api/shifts")
public class CaptainShiftController {

    @Autowired private CaptainGuard             captainGuard;
    @Autowired private ShiftCaptainRepository   shiftCaptainRepository;
    @Autowired private VolunteerShiftRepository volunteerShiftRepository;

    // ── GET /api/shifts/my-team?campaignId=X ─────────────────────────────────

    @GetMapping("/my-team")
    public ResponseEntity<?> getMyTeamShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!captainGuard.isUserCaptain(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Acceso denegado"));
        }

        if (campaignId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "campaignId es obligatorio"));
        }

        Integer userId = captainGuard.extractUserId(authHeader);
        List<Shift> shifts = shiftCaptainRepository.findShiftsByUserIdAndCampaignId(userId, campaignId);

        List<Map<String, Object>> result = shifts.stream()
                .map(shift -> buildShiftMap(shift))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Object> buildShiftMap(Shift shift) {
        List<VolunteerShift> volunteerShifts = volunteerShiftRepository.findByShift(
                shift.getIdCampaign().getId(),
                shift.getIdStore().getId(),
                shift.getShiftDay(),
                shift.getStartTime());

        List<Map<String, Object>> volunteers = volunteerShifts.stream()
                .map(vs -> {
                    Map<String, Object> vm = new HashMap<>();
                    vm.put("volunteerId",   vs.getIdVolunteer().getId());
                    vm.put("volunteerName", vs.getIdVolunteer().getName());
                    vm.put("phone",         vs.getIdVolunteer().getPhone());
                    vm.put("attendance",    vs.getAttendance() != null ? vs.getAttendance() : false);
                    return vm;
                })
                .collect(Collectors.toList());

        Map<String, Object> m = new HashMap<>();
        m.put("shiftId",          shift.getId());
        m.put("campaignId",       shift.getIdCampaign().getId());
        m.put("storeId",          shift.getIdStore().getId());
        m.put("storeName",        shift.getIdStore().getName());
        m.put("day",              shift.getShiftDay()  != null ? shift.getShiftDay().toString()  : null);
        m.put("startTime",        shift.getStartTime() != null ? shift.getStartTime().toString() : null);
        m.put("endTime",          shift.getEndTime()   != null ? shift.getEndTime().toString()   : null);
        m.put("volunteersNeeded", shift.getVolunteersNeeded());
        m.put("observations",     shift.getObservations() != null ? shift.getObservations() : "");
        m.put("volunteers",       volunteers);
        return m;
    }
}
