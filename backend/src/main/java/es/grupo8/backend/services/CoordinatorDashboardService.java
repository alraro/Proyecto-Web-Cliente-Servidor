/**
 * Servicio del panel del coordinador.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 65%
 * - Alejandro Calvo Aguilar: 5%
 * - IA Generativa: 30%
 */
package es.grupo8.backend.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CaptainRequestRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dao.VolunteerRepository;
import es.grupo8.backend.dao.VolunteerShiftRepository;
import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.dto.CampaignEntityDTO;
import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.dto.RegisterResultDTO;
import es.grupo8.backend.dto.StoreResponseDto;
import es.grupo8.backend.dto.UserResponseDto;
import es.grupo8.backend.dto.VoluntarioResponseDto;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.CampaignStore;
import es.grupo8.backend.entity.CaptainRequest;
import es.grupo8.backend.entity.PartnerEntity;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.entity.Volunteer;
import es.grupo8.backend.entity.VolunteerShift;
import es.grupo8.backend.entity.VolunteerShiftId;
import es.grupo8.backend.mapper.CampaignEntityMapper;
import es.grupo8.backend.mapper.CampaignMapper;
import es.grupo8.backend.mapper.PartnerEntityMapper;
import es.grupo8.backend.mapper.StoreMapper;
import es.grupo8.backend.mapper.UserMapper;
import es.grupo8.backend.mapper.VolunteerMapper;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CoordinatorDashboardService {

    private final CoordinatorRepository coordinatorRepository;
    private final CaptainRepository captainRepository;
    private final CaptainRequestRepository captainRequestRepository;
    private final CampaignStoreRepository campaignStoreRepository;
    private final UserRepository userRepository;
    private final VolunteerRepository volunteerRepository;
    private final VolunteerShiftRepository volunteerShiftRepository;
    private final PartnerEntityRepository partnerEntityRepository;

    private final CampaignMapper campaignMapper;
    private final StoreMapper storeMapper;
    private final VolunteerMapper volunteerMapper;
    private final UserMapper userMapper;
    private final PartnerEntityMapper partnerEntityMapper;
    private final CampaignEntityMapper campaignEntityMapper;

    @Transactional(readOnly = true)
    public List<CampaignDTO> getMyCampaigns(Integer userId) {
        return campaignMapper.toDTOList(coordinatorRepository.findCampaignsByUserId(userId));
    }

    @Transactional(readOnly = true)
    public List<StoreResponseDto> getMyStores(Integer campaignId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId es obligatorio");
        }
        return storeMapper.toDTOList(campaignStoreRepository.findStoresByCampaignId(campaignId));
    }

    public List<VoluntarioResponseDto> getVolunteers() {
        return volunteerMapper.toDTOList(volunteerRepository.findAllByOrderByNameAsc());
    }

    public VoluntarioResponseDto createVolunteer(Integer coordinatorId, String name, String phone,
            String email, String address, Integer partnerEntityId) {
        if (name == null) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        Volunteer v = new Volunteer();
        v.setName(name);
        v.setPhone(phone);
        v.setEmail(email);
        v.setAddress(address);

        if (partnerEntityId != null) {
            PartnerEntity pe = partnerEntityRepository.findById(partnerEntityId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Entidad colaboradora no encontrada con id=" + partnerEntityId));
            v.setIdPartnerEntity(pe);
        }

        Volunteer saved = volunteerRepository.save(v);
        return volunteerMapper.toDTO(saved);
    }

    public VoluntarioResponseDto updateVolunteer(Integer coordinatorId, Integer volunteerId,
            String name, String phone, String email, String address, Integer partnerEntityId) {

        Volunteer v = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new NoSuchElementException("Voluntario no encontrado"));
        if (name == null) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        v.setName(name);
        v.setPhone(phone);
        v.setEmail(email);
        v.setAddress(address);

        if (partnerEntityId == null) {
            v.setIdPartnerEntity(null);
        } else {
            PartnerEntity pe = partnerEntityRepository.findById(partnerEntityId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Entidad colaboradora no encontrada con id=" + partnerEntityId));
            v.setIdPartnerEntity(pe);
        }

        Volunteer saved = volunteerRepository.save(v);
        return volunteerMapper.toDTO(saved);
    }

    public void assignVolunteerShift(Integer coordinatorId, Integer volunteerId, Integer campaignId,
            Integer storeId, String shiftDay, String startTime, String endTime) {

        if (volunteerId == null || campaignId == null || storeId == null
                || shiftDay == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException(
                    "Campos obligatorios: volunteerId, campaignId, storeId, shiftDay, startTime, endTime");
        }

        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new IllegalArgumentException("Voluntario no encontrado"));

        if (!campaignStoreRepository.existsByIdCampaign_IdAndIdStore_Id(campaignId, storeId)) {
            throw new IllegalArgumentException("La tienda no está asociada a esta campaña");
        }

        CampaignStore cs = campaignStoreRepository.findByIdCampaign_Id(campaignId).stream()
                .filter(x -> x.getIdStore().getId().equals(storeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Relación campaña-tienda no encontrada"));

        LocalDate day;
        LocalTime start;
        LocalTime end;
        try {
            day   = LocalDate.parse(shiftDay);
            start = LocalTime.parse(startTime);
            end   = LocalTime.parse(endTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de fecha u hora inválido. Use YYYY-MM-DD y HH:mm");
        }

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }

        VolunteerShiftId vsId = new VolunteerShiftId();
        vsId.setIdVolunteer(volunteerId);
        vsId.setIdCampaign(campaignId);
        vsId.setIdStore(storeId);
        vsId.setShiftDay(day);
        vsId.setStartTime(start);

        VolunteerShift vs = new VolunteerShift();
        vs.setId(vsId);
        vs.setIdVolunteer(volunteer);
        vs.setCampaignStores(cs);
        vs.setEndTime(end);
        vs.setAttendance(false);

        volunteerShiftRepository.save(vs);

    }

    public List<UserResponseDto> getCaptains(Integer campaignId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId es obligatorio");
        }
        return userMapper.toDTOList(captainRepository.findUsersByCampaignId(campaignId));
    }

    public RegisterResultDTO registerCaptain(Integer coordinatorId, String name, String email,
            String password, Integer campaignId) {

        if (name == null || email == null || password == null || campaignId == null) {
            throw new IllegalArgumentException("Campos obligatorios: name, email, password, campaignId");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        String normalizedEmail = email.toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException("Ya existe un usuario registrado con ese email");
        }
        if (captainRequestRepository.existsByEmailAndStatus(normalizedEmail, "PENDIENTE")) {
            throw new IllegalStateException("Ya existe una solicitud pendiente para ese email");
        }

        UserEntity coordinator = new UserEntity();
        coordinator.setIdUser(coordinatorId);

        Campaign campaign = new Campaign();
        campaign.setId(campaignId);

        CaptainRequest req = new CaptainRequest();
        req.setName(name);
        req.setEmail(normalizedEmail);
        req.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(10)));
        req.setIdCampaign(campaign);
        req.setIdCoordinator(coordinator);
        req.setStatus("PENDIENTE");

        CaptainRequest saved = captainRequestRepository.save(req);

        RegisterResultDTO result = new RegisterResultDTO();
        result.setMessage("Solicitud enviada. El administrador deberá aprobarla.");
        result.setRequestId(saved.getId());
        return result;
    }

    public List<PartnerEntityResponseDto> getPartnerEntities() {
        return partnerEntityMapper.toDTOList(partnerEntityRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<CampaignEntityDTO> getCampaignEntities(Integer campaignId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId es obligatorio");
        }
        return volunteerShiftRepository.findEntitiesWithVolunteersInCampaign(campaignId).stream()
                .map(pe -> {
                    Long count = volunteerShiftRepository.countVolunteersInCampaignByEntity(campaignId, pe.getId());
                    return campaignEntityMapper.toDTO(pe, count != null ? count : 0L);
                })
                .collect(Collectors.toList());
    }
}
