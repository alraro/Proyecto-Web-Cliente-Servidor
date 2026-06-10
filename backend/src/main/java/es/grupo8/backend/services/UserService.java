/**
 * Servicio de negocio para operaciones de usuarios.
 *
 * Autores:
 * - Alfonso Ramos: 60%
 * - Alejandra Ortiz: 40%
 */
package es.grupo8.backend.services;

import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dao.UserSpecifications;
import es.grupo8.backend.dao.AdminRepository;
import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dao.PartnerEntityManagerRepository;
import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.dto.UserRequestDto;
import es.grupo8.backend.dto.UserResponseDto;
import es.grupo8.backend.dto.UserRoleRequestDto;
import es.grupo8.backend.dto.UserRoleResponseDto;
import es.grupo8.backend.entity.AdminEntity;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.Captain;
import es.grupo8.backend.entity.CaptainId;
import es.grupo8.backend.entity.Coordinator;
import es.grupo8.backend.entity.CoordinatorId;
import es.grupo8.backend.entity.PartnerEntity;
import es.grupo8.backend.entity.PartnerEntityManager;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
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

    public List<UserResponseDto> getAllUsersOrdered() {
        return userMapper.toDTOList(userRepository.findAllByOrderByIdUserAsc());
    }

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

        Specification<UserEntity> spec = Specification
                .where(UserSpecifications.hasSearchTerm(search))
                .and(UserSpecifications.hasRole(role));

        Sort sortObj = Sort.unsorted();
        if (sort != null && !sort.trim().isEmpty()) {
            String[] parts = sort.split(",");
            if (parts.length == 2) {
                Sort.Direction dir = "desc".equalsIgnoreCase(parts[1].trim())
                        ? Sort.Direction.DESC : Sort.Direction.ASC;
                sortObj = Sort.by(dir, "name".equalsIgnoreCase(parts[0].trim()) ? "name" : "idUser");
            }
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<UserEntity> userPage = userRepository.findAll(spec, pageable);

        List<UserResponseDto> content = userPage.getContent().stream()
                .map(userMapper::toDTO)
                .toList();

        return new PaginatedResponse<>(content, page, size,
                userPage.getTotalElements(), userPage.getTotalPages(),
                userPage.hasNext(), userPage.hasPrevious());
    }

    public UserResponseDto getUserById(Integer userId) {
        return userMapper.toDTO(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId)));
    }

    public UserResponseDto createUser(UserRequestDto request) {
        validateCreate(request);

        if (userRepository.existsByEmail(request.getEmail().toLowerCase()))
            throw new IllegalArgumentException("Ya existe un usuario con ese email.");

        UserEntity user = new UserEntity();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        user.setPostalCode(request.getPostalCode() != null ? request.getPostalCode().trim() : null);
        user.setPassword(passwordService.hash(request.getPassword()));

        UserEntity saved = userRepository.save(user);
        return userMapper.toDTO(userRepository.findById(saved.getIdUser())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado tras la creación")));
    }

    public UserResponseDto updateUser(Integer userId, UserRequestDto request) {
        if (request == null) throw new IllegalArgumentException("La petición no es válida.");

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio.");
            if (name.length() > 255) throw new IllegalArgumentException("El nombre no puede superar 255 caracteres.");
            user.setName(name);
        }
        if (request.getEmail() != null) {
            String email = request.getEmail().trim().toLowerCase();
            if (email.isEmpty()) throw new IllegalArgumentException("El email es obligatorio.");
            if (email.length() > 255) throw new IllegalArgumentException("El email no puede superar 255 caracteres.");
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email))
                throw new IllegalArgumentException("Ya existe un usuario con ese email.");
            user.setEmail(email);
        }
        if (request.getPhone() != null) {
            String phone = request.getPhone().trim();
            if (phone.length() > 20) throw new IllegalArgumentException("El teléfono no puede superar 20 caracteres.");
            user.setPhone(phone.isEmpty() ? null : phone);
        }
        if (request.getAddress() != null) {
            String address = request.getAddress().trim();
            user.setAddress(address.isEmpty() ? null : address);
        }
        if (request.getPostalCode() != null) {
            String cp = request.getPostalCode().trim();
            user.setPostalCode(cp.isEmpty() ? null : cp);
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty())
            user.setPassword(passwordService.hash(request.getPassword().trim()));

        return userMapper.toDTO(userRepository.save(user));
    }

    public UserRoleResponseDto assignRole(Integer userId, UserRoleRequestDto request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

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
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
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

    private void validateCreate(UserRequestDto req) {
        if (req == null) throw new IllegalArgumentException("La petición no es válida.");

        String name = req.getName() != null ? req.getName().trim() : "";
        if (name.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio.");
        if (name.length() > 255) throw new IllegalArgumentException("El nombre no puede superar 255 caracteres.");

        String email = req.getEmail() != null ? req.getEmail().trim().toLowerCase() : "";
        if (email.isEmpty()) throw new IllegalArgumentException("El email es obligatorio.");
        if (email.length() > 255) throw new IllegalArgumentException("El email no puede superar 255 caracteres.");

        if (req.getPassword() == null || req.getPassword().trim().isEmpty())
            throw new IllegalArgumentException("La contraseña es obligatoria.");

        if (req.getRole() == null || req.getRole().trim().isEmpty())
            throw new IllegalArgumentException("El rol es obligatorio.");

        String role = req.getRole().trim().toUpperCase(Locale.ROOT);
        if (!List.of("ADMIN", "COORDINATOR", "CAPTAIN", "PARTNER_ENTITY_MANAGER").contains(role))
            throw new IllegalArgumentException("Rol no válido. Los roles válidos son: ADMIN, COORDINATOR, CAPTAIN, PARTNER_ENTITY_MANAGER.");
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
