/**
 * Servicio de consulta de turnos del equipo del capitán.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 85%
 * - IA Generativa: 15%
 */
package es.grupo8.backend.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import es.grupo8.backend.dao.ShiftCaptainRepository;
import es.grupo8.backend.dao.VolunteerShiftRepository;
import es.grupo8.backend.entity.Shift;
import es.grupo8.backend.entity.VolunteerShift;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CaptainShiftService {

    private final ShiftCaptainRepository shiftCaptainRepository;
    private final VolunteerShiftRepository volunteerShiftRepository;

    public List<Map<String, Object>> getMyTeamShifts(Integer userId, Integer campaignId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId es obligatorio");
        }
        return shiftCaptainRepository.findShiftsByUserIdAndCampaignId(userId, campaignId).stream()
                .map(this::buildShiftMap)
                .collect(Collectors.toList());
    }

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
