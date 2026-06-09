package es.grupo8.backend.services;

import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dao.VolunteerRepository;
import es.grupo8.backend.dao.VolunteerShiftRepository;
import es.grupo8.backend.dto.CampaignInfoDto;
import es.grupo8.backend.dto.VoluntarioRequestDto;
import es.grupo8.backend.dto.VoluntarioResponseDto;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.PartnerEntity;
import es.grupo8.backend.entity.Volunteer;
import es.grupo8.backend.mapper.CampaignInfoMapper;
import es.grupo8.backend.mapper.VolunteerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VolunteerService {

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private VolunteerMapper volunteerMapper;

    @Autowired
    private VolunteerShiftRepository volunteerShiftRepository;

    @Autowired
    private CampaignInfoMapper campaignInfoMapper;

    @Autowired
    private PartnerEntityRepository partnerEntityRepository;

    @Autowired
    private UserRepository userRepository;

    public List<VoluntarioResponseDto> getVolunteersByEntity(Integer partnerEntityId) {
        List<Volunteer> volunteers = volunteerRepository.findByIdPartnerEntity_Id(partnerEntityId);
        return volunteerMapper.toDTOList(volunteers);
    }

    public List<CampaignInfoDto> getCampaignsByEntity(Integer partnerEntityId) {
        List<Campaign> campaigns = volunteerShiftRepository.findCampaignsByEntityId(partnerEntityId);
        return campaigns.stream().map(c -> {
            long count = volunteerShiftRepository.countVolunteersInCampaignByEntity(c.getId(), partnerEntityId);
            return campaignInfoMapper.toDTO(c, count);
        }).toList();
    }

    public VoluntarioResponseDto getVolunteerById(Integer id, Integer partnerEntityId) {
        Volunteer volunteer = volunteerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with ID: " + id));

        if (volunteer.getIdPartnerEntity() == null ||
            !volunteer.getIdPartnerEntity().getId().equals(partnerEntityId)) {
            throw new RuntimeException("Volunteer not found for this entity");
        }

        return volunteerMapper.toDTO(volunteer);
    }

    @Transactional
    public VoluntarioResponseDto createVolunteer(VoluntarioRequestDto request, Integer partnerEntityId) {
        validateRequest(request);

        PartnerEntity partnerEntity = partnerEntityRepository.findById(partnerEntityId)
                .orElseThrow(() -> new RuntimeException("Partner entity not found with ID: " + partnerEntityId));

        Volunteer volunteer = new Volunteer();
        volunteer.setName(UtilsService.trimToNull(request.name()));
        volunteer.setPhone(normalizePhone(request.phone()));
        volunteer.setEmail(UtilsService.trimToNull(request.email()));
        volunteer.setAddress(UtilsService.trimToNull(request.address()));
        volunteer.setIdPartnerEntity(partnerEntity);

        Volunteer savedVolunteer = volunteerRepository.save(volunteer);
        return volunteerMapper.toDTO(savedVolunteer);
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

        volunteer.setName(UtilsService.trimToNull(request.name()));
        volunteer.setPhone(normalizePhone(request.phone()));
        volunteer.setEmail(UtilsService.trimToNull(request.email()));
        volunteer.setAddress(UtilsService.trimToNull(request.address()));

        Volunteer updatedVolunteer = volunteerRepository.save(volunteer);
        return volunteerMapper.toDTO(updatedVolunteer);
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

    public boolean canAccessPartnerEntity(Integer userId, Integer partnerEntityId) {
        try {
            return userRepository.isAdmin(userId) || userRepository.isPartnerEntityManagerOfEntity(userId, partnerEntityId);
        } catch (Exception e) {
            return false;
        }
    }

    private String normalizePhone(String phone) {
        String trimmed = UtilsService.trimToNull(phone);
        if (trimmed == null) {
            return null;
        }
        return trimmed.replaceAll("\\s+", " ");
    }
}