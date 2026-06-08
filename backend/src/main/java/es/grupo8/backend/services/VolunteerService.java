package es.grupo8.backend.services;

import es.grupo8.backend.dao.PartnerEntityManagerRepository;
import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dao.VolunteerRepository;
import es.grupo8.backend.dto.VoluntarioRequestDto;
import es.grupo8.backend.dto.VoluntarioResponseDto;
import es.grupo8.backend.dto.VoluntarioResponseDto.CampaignInfo;
import es.grupo8.backend.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VolunteerService {

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private PartnerEntityManagerRepository partnerEntityManagerRepository;

    @Autowired
    private PartnerEntityRepository partnerEntityRepository;

    @Autowired
    private UserRepository userRepository;

    public List<VoluntarioResponseDto> getVolunteersByEntity(Integer partnerEntityId) {
        List<Volunteer> volunteers = volunteerRepository.findByIdPartnerEntity_Id(partnerEntityId);
        return volunteers.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public VoluntarioResponseDto getVolunteerById(Integer id, Integer partnerEntityId) {
        Volunteer volunteer = volunteerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with ID: " + id));

        if (volunteer.getIdPartnerEntity() == null ||
            !volunteer.getIdPartnerEntity().getId().equals(partnerEntityId)) {
            throw new RuntimeException("Volunteer not found for this entity");
        }

        return toResponseDto(volunteer);
    }

    @Transactional
    public VoluntarioResponseDto createVolunteer(VoluntarioRequestDto request, Integer partnerEntityId) {
        validateRequest(request);

        PartnerEntity partnerEntity = partnerEntityRepository.findById(partnerEntityId)
                .orElseThrow(() -> new RuntimeException("Partner entity not found with ID: " + partnerEntityId));

        Volunteer volunteer = new Volunteer();
        volunteer.setName(trimToNull(request.name()));
        volunteer.setPhone(normalizePhone(request.phone()));
        volunteer.setEmail(trimToNull(request.email()));
        volunteer.setAddress(trimToNull(request.address()));
        volunteer.setIdPartnerEntity(partnerEntity);

        Volunteer savedVolunteer = volunteerRepository.save(volunteer);
        return toResponseDto(savedVolunteer);
    }

    @Transactional
    public VoluntarioResponseDto updateVolunteer(Integer id, VoluntarioRequestDto request, Integer partnerEntityId) {
        validateRequest(request);

        Volunteer volunteer = volunteerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with ID: " + id));

        if (volunteer.getIdPartnerEntity() == null ||
            !volunteer.getIdPartnerEntity().getId().equals(partnerEntityId)) {
            throw new RuntimeException("Volunteer not found for this entity");
        }

        volunteer.setName(trimToNull(request.name()));
        volunteer.setPhone(normalizePhone(request.phone()));
        volunteer.setEmail(trimToNull(request.email()));
        volunteer.setAddress(trimToNull(request.address()));

        Volunteer updatedVolunteer = volunteerRepository.save(volunteer);
        return toResponseDto(updatedVolunteer);
    }

    @Transactional
    public void deleteVolunteer(Integer id, Integer partnerEntityId) {
        Volunteer volunteer = volunteerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with ID: " + id));

        if (volunteer.getIdPartnerEntity() == null ||
            !volunteer.getIdPartnerEntity().getId().equals(partnerEntityId)) {
            throw new RuntimeException("Volunteer not found for this entity");
        }

        volunteerRepository.delete(volunteer);
    }

    private void validateRequest(VoluntarioRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es inválida.");
        }

        String name = request.name() == null ? "" : request.name().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("El nombre no puede superar 255 caracteres.");
        }

        if (request.email() != null && !request.email().trim().isEmpty()) {
            String email = request.email().trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("El formato del email es inválido.");
            }
        }
    }

    public boolean verifyEntityAccess(Integer userId, Integer partnerEntityId) {
        try {
            return userRepository.isAdmin(userId) || userRepository.isPartnerEntityManagerOfEntity(userId, partnerEntityId);
        } catch (Exception e) {
            return false;
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizePhone(String phone) {
        String trimmed = trimToNull(phone);
        if (trimmed == null) {
            return null;
        }
        return trimmed.replaceAll("\\s+", " ");
    }

    private VoluntarioResponseDto toResponseDto(Volunteer volunteer) {
        List<CampaignInfo> campaigns = new ArrayList<>();

        Set<VolunteerShift> shifts = volunteer.getVolunteerShifts();
        if (shifts != null) {
            Set<Integer> addedCampaignIds = new java.util.HashSet<>();
            for (VolunteerShift shift : shifts) {
                if (shift.getCampaignStores() != null &&
                    shift.getCampaignStores().getIdCampaign() != null) {

                    Campaign campaign = shift.getCampaignStores().getIdCampaign();
                    if (addedCampaignIds.add(campaign.getId())) {
                        campaigns.add(new CampaignInfo(campaign.getId(), campaign.getName()));
                    }
                }
            }
        }

        Integer entityId = null;
        String entityName = null;
        if (volunteer.getIdPartnerEntity() != null) {
            entityId = volunteer.getIdPartnerEntity().getId();
            entityName = volunteer.getIdPartnerEntity().getName();
        }

        return new VoluntarioResponseDto(
                volunteer.getId(),
                volunteer.getName(),
                volunteer.getPhone(),
                volunteer.getEmail(),
                volunteer.getAddress(),
                entityId,
                entityName,
                campaigns
        );
    }
}