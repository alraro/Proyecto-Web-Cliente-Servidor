package es.grupo8.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.VolunteerRepository;
import es.grupo8.backend.dao.VolunteerShiftRepository;
import es.grupo8.backend.dto.AssignmentResponseDTO;
import es.grupo8.backend.dto.CaptainAssignmentRequestDTO;
import es.grupo8.backend.dto.ShiftAssignmentRequestDTO;
import es.grupo8.backend.entity.CampaignStore;
import es.grupo8.backend.entity.CampaignStoreId;
import es.grupo8.backend.entity.Captain;
import es.grupo8.backend.entity.CaptainId;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.entity.Volunteer;
import es.grupo8.backend.entity.VolunteerShift;
import es.grupo8.backend.entity.VolunteerShiftId;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ShiftAssignmentService {

    @Autowired
    private VolunteerShiftRepository volunteerShiftRepository;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private CaptainRepository captainRepository;

    @Autowired
    private CampaignStoreRepository campaignStoreRepository;

    @Value("${app.shift.max-volunteers:10}")
    private int maxVolunteersPerShift;

    /**
     * Check for shift overlap for a volunteer on a specific day
     * Two shifts overlap if: start1 < end2 AND start2 < end1
     */
    public boolean checkShiftOverlap(Integer volunteerId, Integer campaignId, 
                                     Integer storeId, LocalDate shiftDay,
                                     LocalTime newStartTime, LocalTime newEndTime) {
        List<VolunteerShift> existingShifts = volunteerShiftRepository.findByIdIdVolunteerAndIdIdCampaignAndIdIdStoreAndIdShiftDay(
                volunteerId, campaignId, storeId, shiftDay);

        for (VolunteerShift shift : existingShifts) {
            LocalTime existingStart = shift.getId().getStartTime();
            LocalTime existingEnd = shift.getEndTime();

            // Check overlap: newStart < existingEnd AND existingStart < newEnd
            if (newStartTime.isBefore(existingEnd) && existingStart.isBefore(newEndTime)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for shift overlap across all stores for the same day
     */
    public boolean checkShiftOverlapAnyStore(Integer volunteerId, Integer campaignId,
                                             LocalDate shiftDay, LocalTime newStartTime, 
                                             LocalTime newEndTime) {
        List<VolunteerShift> existingShifts = volunteerShiftRepository.findByIdIdVolunteerAndIdIdCampaignAndIdShiftDay(
                volunteerId, campaignId, shiftDay);

        for (VolunteerShift shift : existingShifts) {
            LocalTime existingStart = shift.getId().getStartTime();
            LocalTime existingEnd = shift.getEndTime();

            if (newStartTime.isBefore(existingEnd) && existingStart.isBefore(newEndTime)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate maximum volunteers per shift
     */
    public boolean validateMaxVolunteers(Integer campaignId, Integer storeId, 
                                         LocalDate shiftDay, LocalTime startTime) {
        long currentCount = volunteerShiftRepository.countByIdIdCampaignAndIdIdStoreAndIdShiftDayAndIdStartTime(
                campaignId, storeId, shiftDay, startTime);
        return currentCount < maxVolunteersPerShift;
    }

    /**
     * Get current volunteer count for a shift
     */
    public long getCurrentVolunteerCount(Integer campaignId, Integer storeId,
                                         LocalDate shiftDay, LocalTime startTime) {
        return volunteerShiftRepository.countByIdIdCampaignAndIdIdStoreAndIdShiftDayAndIdStartTime(
                campaignId, storeId, shiftDay, startTime);
    }

    /**
     * Assign volunteer to a shift
     */
    @Transactional
    public AssignmentResponseDTO assignVolunteerToShift(ShiftAssignmentRequestDTO request) {
        // Validate volunteer exists
        Optional<Volunteer> volunteerOpt = volunteerRepository.findById(request.getVolunteerId());
        if (volunteerOpt.isEmpty()) {
            return new AssignmentResponseDTO(false, "Volunteer not found", "NOT_FOUND");
        }

        // Check shift overlap
        if (checkShiftOverlapAnyStore(request.getVolunteerId(), request.getCampaignId(),
                request.getShiftDay(), request.getStartTime(), request.getEndTime())) {
            return new AssignmentResponseDTO(false, 
                    "Shift overlap detected: The volunteer already has an assigned shift that overlaps with this time slot",
                    "SHIFT_OVERLAP");
        }

        // Check max volunteers
        if (!validateMaxVolunteers(request.getCampaignId(), request.getStoreId(),
                request.getShiftDay(), request.getStartTime())) {
            long currentCount = getCurrentVolunteerCount(request.getCampaignId(), request.getStoreId(),
                    request.getShiftDay(), request.getStartTime());
            return new AssignmentResponseDTO(false,
                    "Maximum volunteers exceeded for this shift. Current: " + currentCount + 
                    ", Maximum: " + maxVolunteersPerShift,
                    "MAX_VOLUNTEERS");
        }

        // Create campaign store key if not exists
        CampaignStoreId csId = new CampaignStoreId();
        csId.setIdCampaign(request.getCampaignId());
        csId.setIdStore(request.getStoreId());

        Optional<CampaignStore> csOpt = campaignStoreRepository.findById(csId);
        if (csOpt.isEmpty()) {
            return new AssignmentResponseDTO(false, "Campaign store not found", "NOT_FOUND");
        }

        // Create volunteer shift
        VolunteerShiftId vsId = new VolunteerShiftId();
        vsId.setIdVolunteer(request.getVolunteerId());
        vsId.setIdCampaign(request.getCampaignId());
        vsId.setIdStore(request.getStoreId());
        vsId.setShiftDay(request.getShiftDay());
        vsId.setStartTime(request.getStartTime());

        VolunteerShift volunteerShift = new VolunteerShift();
        volunteerShift.setId(vsId);
        volunteerShift.setIdVolunteer(volunteerOpt.get());
        volunteerShift.setCampaignStores(csOpt.get());
        volunteerShift.setEndTime(request.getEndTime());
        volunteerShift.setAttendance(false);

        volunteerShiftRepository.save(volunteerShift);

        return new AssignmentResponseDTO(true, "Volunteer successfully assigned to shift");
    }

    /**
     * Unassign volunteer from a shift
     */
    @Transactional
    public AssignmentResponseDTO unassignVolunteerFromShift(Integer volunteerId, Integer campaignId,
                                                            Integer storeId, LocalDate shiftDay, 
                                                            LocalTime startTime) {
        VolunteerShiftId id = new VolunteerShiftId();
        id.setIdVolunteer(volunteerId);
        id.setIdCampaign(campaignId);
        id.setIdStore(storeId);
        id.setShiftDay(shiftDay);
        id.setStartTime(startTime);

        Optional<VolunteerShift> shiftOpt = volunteerShiftRepository.findById(id);
        if (shiftOpt.isEmpty()) {
            return new AssignmentResponseDTO(false, "Assignment not found", "NOT_FOUND");
        }

        volunteerShiftRepository.delete(shiftOpt.get());
        return new AssignmentResponseDTO(true, "Volunteer successfully unassigned from shift");
    }

    /**
     * Assign captain to a campaign
     */
    @Transactional
    public AssignmentResponseDTO assignCaptainToCampaign(CaptainAssignmentRequestDTO request) {
        // Validate user exists
        Optional<UserEntity> userOpt = volunteerRepository.findUserById(request.getUserId());
        if (userOpt.isEmpty()) {
            return new AssignmentResponseDTO(false, "User not found", "NOT_FOUND");
        }

        // Check if already captain
        CaptainId captainId = new CaptainId();
        captainId.setIdUser(request.getUserId());
        captainId.setIdCampaign(request.getCampaignId());

        if (captainRepository.existsById(captainId)) {
            return new AssignmentResponseDTO(false, "User is already a captain for this campaign", "ALREADY_ASSIGNED");
        }

        Captain captain = new Captain();
        captain.setId(captainId);
        captain.setIdUser(userOpt.get());

        // Get campaign entity
        CampaignStoreId csId = new CampaignStoreId();
        csId.setIdCampaign(request.getCampaignId());
        csId.setIdStore(1); // Dummy store - we need campaign, not campaign_store

        captainRepository.save(captain);

        return new AssignmentResponseDTO(true, "Captain successfully assigned to campaign");
    }

    /**
     * Unassign captain from a campaign
     */
    @Transactional
    public AssignmentResponseDTO unassignCaptainFromCampaign(Integer userId, Integer campaignId) {
        CaptainId id = new CaptainId();
        id.setIdUser(userId);
        id.setIdCampaign(campaignId);

        Optional<Captain> captainOpt = captainRepository.findById(id);
        if (captainOpt.isEmpty()) {
            return new AssignmentResponseDTO(false, "Captain assignment not found", "NOT_FOUND");
        }

        captainRepository.delete(captainOpt.get());
        return new AssignmentResponseDTO(true, "Captain successfully unassigned from campaign");
    }

    /**
     * Get all volunteers available for assignment
     */
    public List<Volunteer> getAvailableVolunteers() {
        return volunteerRepository.findAll();
    }

    /**
     * Get all shifts for a specific campaign, store, day
     */
    public List<VolunteerShift> getShiftsForCampaignStoreDay(Integer campaignId, Integer storeId, 
                                                              LocalDate shiftDay) {
        return volunteerShiftRepository.findByIdIdCampaignAndIdIdStoreAndIdShiftDay(
                campaignId, storeId, shiftDay);
    }

    public int getMaxVolunteersPerShift() {
        return maxVolunteersPerShift;
    }
}