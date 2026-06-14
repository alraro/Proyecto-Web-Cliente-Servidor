package es.grupo8.backend.services;

import es.grupo8.backend.dao.*;
import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.dto.UserResponseDto;
import es.grupo8.backend.dto.UserRoleRequestDto;
import es.grupo8.backend.dto.UserRoleResponseDto;
import es.grupo8.backend.entity.*;
import es.grupo8.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private AdminRepository adminRepository;
    @Autowired private CampaignRepository campaignRepository;
    @Autowired private CaptainRepository captainRepository;
    @Autowired private CoordinatorRepository coordinatorRepository;
    @Autowired private PartnerEntityRepository partnerEntityRepository;
    @Autowired private PartnerEntityManagerRepository partnerEntityManagerRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private PasswordService passwordService;
    @Autowired private UserMapper userMapper;

    @Autowired
    private AuthService authService;

    public List<UserResponseDto> getPendingUsersOrdered() {
        return userRepository.findAllByOrderByIdUserAsc().stream()
                .filter(user -> "PENDIENTE".equals(userMapper.resolveRole(user.getIdUser())))
                .map(userMapper::toDTO)
                .toList();
    }

    public PaginatedResponse<UserResponseDto> getAllUsers(
            int page, int size, String sort, String search, String role) {

        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, 100));
        int offset = page * size;

        String sortField = "id";
        String sortDir = "asc";
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            sortField = parts[0].trim().toLowerCase();
            sortDir = parts.length > 1 && "desc".equals(parts[1].trim().toLowerCase()) ? "desc" : "asc";
        }

        List<UserEntity> users = switch (sortField) {
            case "name" -> sortDir.equals("asc")
                    ? userRepository.findAllByNameAsc(search, role, size, offset)
                    : userRepository.findAllByNameDesc(search, role, size, offset);
            default -> sortDir.equals("asc")
                    ? userRepository.findAllByIdAsc(search, role, size, offset)
                    : userRepository.findAllByIdDesc(search, role, size, offset);
        };

        long total = userRepository.countUsers(search, role);
        int totalPages = (int) Math.ceil((double) total / size);

        List<UserResponseDto> content = userMapper.toDTOList(users);

        return new PaginatedResponse<>(content, page, size,
                total, totalPages,
                page < totalPages - 1,
                page > 0);
    }

    public UserRoleResponseDto assignRole(Integer userId, UserRoleRequestDto request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        String role = normalizeRole(request == null ? null : request.getRole());
        if (role == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }

        removeExistingRoles(userId);
        assignNewRole(user, role);

        return new UserRoleResponseDto("Rol asignado", userId, role);
    }

    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId))
            throw new NoSuchElementException("Usuario no encontrado con ID: " + userId);
        userRepository.deleteById(userId);
    }

    private String normalizeRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return null;
        }
        return Normalizer.normalize(rawRole.trim().toUpperCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private void removeExistingRoles(Integer userId) {
        adminRepository.findById(userId).ifPresent(adminRepository::delete);

        coordinatorRepository.findAll().stream()
                .filter(coordinator -> coordinator.getId().getIdUser().equals(userId))
                .forEach(coordinator -> coordinatorRepository.deleteByIdIdUserAndIdIdCampaign(
                        coordinator.getId().getIdUser(),
                        coordinator.getId().getIdCampaign()));

        captainRepository.findAll().stream()
                .filter(captain -> captain.getId().getIdUser().equals(userId))
                .forEach(captain -> captainRepository.deleteByIdIdUserAndIdIdCampaign(
                        captain.getId().getIdUser(),
                        captain.getId().getIdCampaign()));

        partnerEntityManagerRepository.findById(userId).ifPresent(partnerEntityManagerRepository::delete);

        storeRepository.findByIdResponsible_IdUser(userId).ifPresent(store -> {
            store.setIdResponsible(null);
            storeRepository.save(store);
        });
    }

    private void assignNewRole(UserEntity user, String role) {
        switch (role) {
            case "ADMINISTRADOR" -> assignAdmin(user);
            case "COORDINADOR" -> assignCoordinator(user);
            case "CAPITAN" -> assignCaptain(user);
            case "COLABORADOR" -> assignPartnerEntityManager(user);
            case "RESPONSABLE_TIENDA" -> assignResponsibleStore(user);
            default -> throw new IllegalArgumentException("Rol no soportado: " + role);
        }
    }

    private void assignAdmin(UserEntity user) {
        if (!adminRepository.existsByIdUser(user.getIdUser())) {
            AdminEntity admin = new AdminEntity();
            admin.setIdUser(user.getIdUser());
            adminRepository.save(admin);
        }
    }

    private void assignCoordinator(UserEntity user) {
        Campaign campaign = getFirstCampaign();
        Integer campaignId = campaign.getId();
        if (!coordinatorRepository.existsByIdIdUserAndIdIdCampaign(user.getIdUser(), campaignId)) {
            CoordinatorId coordinatorId = new CoordinatorId();
            coordinatorId.setIdUser(user.getIdUser());
            coordinatorId.setIdCampaign(campaignId);

            Coordinator coordinator = new Coordinator();
            coordinator.setId(coordinatorId);
            coordinator.setIdUser(user);
            coordinator.setIdCampaign(campaign);
            coordinatorRepository.save(coordinator);
        }
    }

    private void assignCaptain(UserEntity user) {
        Campaign campaign = getFirstCampaign();
        Integer campaignId = campaign.getId();
        if (!captainRepository.existsByIdIdUserAndIdIdCampaign(user.getIdUser(), campaignId)) {
            CaptainId captainId = new CaptainId();
            captainId.setIdUser(user.getIdUser());
            captainId.setIdCampaign(campaignId);

            Captain captain = new Captain();
            captain.setId(captainId);
            captain.setIdUser(user);
            captain.setIdCampaign(campaign);
            captainRepository.save(captain);
        }
    }

    private void assignPartnerEntityManager(UserEntity user) {
        PartnerEntity partnerEntity = partnerEntityRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay entidades colaboradoras disponibles para asignar este rol."));

        if (!partnerEntityManagerRepository.existsById(user.getIdUser())) {
            PartnerEntityManager manager = new PartnerEntityManager();
            manager.setUserAccounts(user);
            manager.setIdPartnerEntity(partnerEntity);
            partnerEntityManagerRepository.save(manager);
        }
    }

    private void assignResponsibleStore(UserEntity user) {
        Store store = storeRepository.findAllByOrderByIdAsc().stream()
                .filter(candidate -> candidate.getIdResponsible() == null)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay tiendas sin responsable disponibles."));

        store.setIdResponsible(user);
        storeRepository.save(store);
    }

    private Campaign getFirstCampaign() {
        Optional<Campaign> campaign = campaignRepository.findAll().stream().findFirst();
        return campaign.orElseThrow(() -> new IllegalArgumentException("No hay campañas disponibles para asignar este rol."));
    }

    public boolean isAdmin(Integer userId) {
        try {
            return userRepository.isAdmin(userId);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAdminFromToken(String token) {
        Integer userId = authService.extractUserIdFromToken(token);
        return isAdmin(userId);
    }

    public boolean isCoordinator(Integer userId) {
        try {
            return userRepository.isCoordinator(userId);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCoordinatorFromToken(String token) {
        Integer userId = authService.extractUserIdFromToken(token);
        return isCoordinator(userId);
    }

    public boolean isCaptain(Integer userId) {
        try {
            return userRepository.isCaptain(userId);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCaptainFromToken(String token) {
        Integer userId = authService.extractUserIdFromToken(token);
        return isCaptain(userId);
    }

    public boolean isPartnerEntityManager(Integer userId) {
        try {
            return userRepository.isPartnerEntityManager(userId);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPartnerEntityManagerFromToken(String token) {
        Integer userId = authService.extractUserIdFromToken(token);
        return isPartnerEntityManager(userId);
    }

    public boolean isManagerOfEntity(Integer userId, Integer entityId) {
        try {
            return userRepository.isPartnerEntityManagerOfEntity(userId, entityId);
        } catch (Exception e) {
            return false;
        }
    }
}
