/**
 * Servicio para la gestión de asignaciones de turnos.
 *
 * Autores:
 * - Alejandro Calvo Aguilar: 70%
 * - IA Generativa: 30%
 */

package es.grupo8.backend.services;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.ShiftCaptainRepository;
import es.grupo8.backend.dao.ShiftRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dao.VolunteerRepository;
import es.grupo8.backend.dao.VolunteerShiftRepository;
import es.grupo8.backend.dto.AttendanceRequestDto;
import es.grupo8.backend.dto.AttendanceResponseDto;
import es.grupo8.backend.dto.AvailableCaptainDto;
import es.grupo8.backend.dto.AvailableVolunteerDto;
import es.grupo8.backend.dto.CaptainAssignResponseDto;
import es.grupo8.backend.dto.CaptainAssignmentDto;
import es.grupo8.backend.dto.CaptainShiftAssignRequestDto;
import es.grupo8.backend.dto.ShiftCaptainsResponseDto;
import es.grupo8.backend.dto.ShiftVolunteersResponseDto;
import es.grupo8.backend.dto.VolunteerAssignResponseDto;
import es.grupo8.backend.dto.VolunteerShiftAssignRequestDto;
import es.grupo8.backend.entity.CampaignStore;
import es.grupo8.backend.entity.CampaignStoreId;
import es.grupo8.backend.entity.Shift;
import es.grupo8.backend.entity.ShiftCaptain;
import es.grupo8.backend.entity.ShiftCaptainId;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.entity.Volunteer;
import es.grupo8.backend.entity.VolunteerShift;
import es.grupo8.backend.entity.VolunteerShiftId;
import es.grupo8.backend.exceptions.ShiftConflictException;
import es.grupo8.backend.mapper.AvailableCaptainMapper;
import es.grupo8.backend.mapper.AvailableVolunteerMapper;
import es.grupo8.backend.mapper.CaptainAssignmentMapper;
import es.grupo8.backend.mapper.VolunteerAssignmentMapper;

@Service
public class ShiftAssignmentService {

    @Autowired private ShiftRepository shiftRepository;
    @Autowired private VolunteerRepository volunteerRepository;
    @Autowired private VolunteerShiftRepository volunteerShiftRepository;
    @Autowired private ShiftCaptainRepository shiftCaptainRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CampaignStoreRepository campaignStoreRepository;

    @Autowired private VolunteerAssignmentMapper volunteerAssignmentMapper;
    @Autowired private CaptainAssignmentMapper captainAssignmentMapper;
    @Autowired private AvailableVolunteerMapper availableVolunteerMapper;
    @Autowired private AvailableCaptainMapper availableCaptainMapper;

    public ShiftVolunteersResponseDto getVolunteers(Integer shiftId) {
        Shift shift = findShift(shiftId);

        List<VolunteerShift> assignments = volunteerShiftRepository.findByShift(
                shift.getIdCampaign().getId(),
                shift.getIdStore().getId(),
                shift.getShiftDay(),
                shift.getStartTime());

        ShiftVolunteersResponseDto dto = new ShiftVolunteersResponseDto();
        dto.setShiftId(shiftId);
        dto.setVolunteersNeeded(shift.getVolunteersNeeded());
        dto.setVolunteersAssigned(assignments.size());
        dto.setVolunteers(volunteerAssignmentMapper.toDTOList(assignments));
        return dto;
    }

    public VolunteerAssignResponseDto assignVolunteer(Integer shiftId, VolunteerShiftAssignRequestDto request) {
        if (request == null || request.getVolunteerId() == null) {
            throw new IllegalArgumentException("volunteerId es obligatorio");
        }

        Shift shift = findShift(shiftId);
        Integer volunteerId = request.getVolunteerId();

        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new NoSuchElementException("Voluntario no encontrado"));

        VolunteerShiftId vsId = buildVolunteerShiftId(volunteerId, shift);
        if (volunteerShiftRepository.existsById(vsId)) {
            throw new IllegalStateException("El voluntario ya está asignado a este turno");
        }

        long currentCount = volunteerShiftRepository.countByShift(
                shift.getIdCampaign().getId(),
                shift.getIdStore().getId(),
                shift.getShiftDay(),
                shift.getStartTime());

        if (currentCount >= shift.getVolunteersNeeded()) {
            throw new ShiftConflictException(
                    "Aforo completo: este turno ya tiene el máximo de voluntarios (" + shift.getVolunteersNeeded() + ")",
                    "CAPACITY_EXCEEDED");
        }

        List<VolunteerShift> overlaps = volunteerShiftRepository.findOverlappingForVolunteer(
                volunteerId,
                shift.getShiftDay(),
                shift.getStartTime(),
                shift.getEndTime());

        if (!overlaps.isEmpty()) {
            VolunteerShift overlap = overlaps.get(0);
            throw new ShiftConflictException(
                    "El voluntario ya tiene un turno solapado ese día: " +
                    overlap.getId().getStartTime() + " – " + overlap.getEndTime(),
                    "OVERLAP");
        }

        CampaignStoreId csId = new CampaignStoreId();
        csId.setIdCampaign(shift.getIdCampaign().getId());
        csId.setIdStore(shift.getIdStore().getId());
        CampaignStore campaignStore = campaignStoreRepository.findById(csId)
                .orElseThrow(() -> new IllegalArgumentException("La tienda ya no está asociada a esta campaña"));

        VolunteerShift vs = new VolunteerShift();
        vs.setId(vsId);
        vs.setIdVolunteer(volunteer);
        vs.setCampaignStores(campaignStore);
        vs.setEndTime(shift.getEndTime());
        vs.setAttendance(false);
        volunteerShiftRepository.save(vs);

        VolunteerAssignResponseDto dto = new VolunteerAssignResponseDto();
        dto.setMessage("Voluntario asignado correctamente");
        dto.setShiftId(shiftId);
        dto.setVolunteerId(volunteerId);
        dto.setVolunteerName(volunteer.getName());
        return dto;
    }

    public void unassignVolunteer(Integer shiftId, Integer volunteerId) {
        Shift shift = findShift(shiftId);

        VolunteerShiftId vsId = buildVolunteerShiftId(volunteerId, shift);
        if (!volunteerShiftRepository.existsById(vsId)) {
            throw new IllegalStateException("El voluntario no está asignado a este turno");
        }

        volunteerShiftRepository.deleteById(vsId);
    }

    public ShiftCaptainsResponseDto getCaptains(Integer shiftId) {
        findShift(shiftId);

        List<CaptainAssignmentDto> captains = shiftCaptainRepository.findByShift_Id(shiftId)
                .stream()
                .map(captainAssignmentMapper::toDTO)
                .collect(Collectors.toList());

        ShiftCaptainsResponseDto dto = new ShiftCaptainsResponseDto();
        dto.setShiftId(shiftId);
        dto.setCaptains(captains);
        return dto;
    }

    public CaptainAssignResponseDto assignCaptain(Integer shiftId, CaptainShiftAssignRequestDto request) {
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("userId es obligatorio");
        }

        Shift shift = findShift(shiftId);
        Integer userId = request.getUserId();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        ShiftCaptainId scId = new ShiftCaptainId();
        scId.setIdShift(shiftId);
        scId.setIdUser(userId);
        if (shiftCaptainRepository.existsById(scId)) {
            throw new IllegalStateException("El capitán ya está asignado a este turno");
        }

        List<ShiftCaptain> overlaps = shiftCaptainRepository.findOverlappingForCaptain(
                userId,
                shift.getShiftDay(),
                shift.getStartTime(),
                shift.getEndTime(),
                shiftId);

        if (!overlaps.isEmpty()) {
            ShiftCaptain overlap = overlaps.get(0);
            throw new ShiftConflictException(
                    "El capitán ya tiene un turno solapado ese día: " +
                    overlap.getShift().getStartTime() + " – " + overlap.getShift().getEndTime(),
                    "OVERLAP");
        }

        ShiftCaptain sc = new ShiftCaptain();
        sc.setId(scId);
        sc.setShift(shift);
        sc.setUser(user);
        shiftCaptainRepository.save(sc);

        CaptainAssignResponseDto dto = new CaptainAssignResponseDto();
        dto.setMessage("Capitán asignado correctamente");
        dto.setShiftId(shiftId);
        dto.setUserId(userId);
        dto.setUserName(user.getName());
        return dto;
    }

    public void unassignCaptain(Integer shiftId, Integer userId) {
        findShift(shiftId);

        ShiftCaptainId scId = new ShiftCaptainId();
        scId.setIdShift(shiftId);
        scId.setIdUser(userId);
        if (!shiftCaptainRepository.existsById(scId)) {
            throw new IllegalStateException("El capitán no está asignado a este turno");
        }

        shiftCaptainRepository.deleteById(scId);
    }

    public AttendanceResponseDto updateAttendance(Integer shiftId, AttendanceRequestDto request) {
        if (request == null || request.getVolunteerId() == null) {
            throw new IllegalArgumentException("volunteerId es obligatorio");
        }
        if (request.getAttendance() == null) {
            throw new IllegalArgumentException("attendance es obligatorio");
        }

        Shift shift = findShift(shiftId);

        VolunteerShiftId vsId = buildVolunteerShiftId(request.getVolunteerId(), shift);
        VolunteerShift vs = volunteerShiftRepository.findById(vsId)
                .orElseThrow(() -> new NoSuchElementException("El voluntario no está asignado a este turno"));

        vs.setAttendance(request.getAttendance());
        volunteerShiftRepository.save(vs);

        AttendanceResponseDto dto = new AttendanceResponseDto();
        dto.setMessage("Asistencia actualizada correctamente");
        dto.setShiftId(shiftId);
        dto.setVolunteerId(request.getVolunteerId());
        dto.setAttendance(request.getAttendance());
        return dto;
    }

    public List<AvailableVolunteerDto> getAvailableVolunteers(Integer shiftId) {
        Shift shift = findShift(shiftId);

        List<Integer> alreadyAssigned = volunteerShiftRepository.findByShift(
                shift.getIdCampaign().getId(),
                shift.getIdStore().getId(),
                shift.getShiftDay(),
                shift.getStartTime())
                .stream()
                .map(vs -> vs.getIdVolunteer().getId())
                .collect(Collectors.toList());

        return volunteerRepository.findAll().stream()
                .filter(v -> !alreadyAssigned.contains(v.getId()))
                .map(availableVolunteerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<AvailableCaptainDto> getAvailableCaptains(Integer shiftId) {
        findShift(shiftId);

        List<Integer> alreadyAssigned = shiftCaptainRepository.findByShift_Id(shiftId)
                .stream()
                .map(sc -> sc.getUser().getIdUser())
                .collect(Collectors.toList());

        return userRepository.findAllCaptains().stream()
                .filter(u -> !alreadyAssigned.contains(u.getIdUser()))
                .map(availableCaptainMapper::toDTO)
                .collect(Collectors.toList());
    }

    private Shift findShift(Integer shiftId) {
        return shiftRepository.findById(shiftId)
                .orElseThrow(() -> new NoSuchElementException("Turno no encontrado"));
    }

    private VolunteerShiftId buildVolunteerShiftId(Integer volunteerId, Shift shift) {
        VolunteerShiftId id = new VolunteerShiftId();
        id.setIdVolunteer(volunteerId);
        id.setIdCampaign(shift.getIdCampaign().getId());
        id.setIdStore(shift.getIdStore().getId());
        id.setShiftDay(shift.getShiftDay());
        id.setStartTime(shift.getStartTime());
        return id;
    }
}
