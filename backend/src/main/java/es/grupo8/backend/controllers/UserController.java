package es.grupo8.backend.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dao.AdminRepository;
import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dao.PartnerEntityManagerRepository;
import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dao.UserRepository;
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
import es.grupo8.backend.security.AdminGuard;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    @Autowired private UserRepository  userRepository;
    @Autowired private AdminRepository adminRepository;
    @Autowired private CampaignRepository campaignRepository;
    @Autowired private CaptainRepository captainRepository;
    @Autowired private CoordinatorRepository coordinatorRepository;
    @Autowired private PartnerEntityRepository partnerEntityRepository;
    @Autowired private PartnerEntityManagerRepository partnerEntityManagerRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private AdminGuard      adminGuard;

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();

        List<Map<String, Object>> users = userRepository.findAll()
                .stream()
                .map(u -> toMap(u, resolveRole(u.getIdUser())))
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();

        List<Map<String, Object>> pending = userRepository.findAll()
                .stream()
                .filter(u -> "PENDIENTE".equals(resolveRole(u.getIdUser())))
                .map(u -> toMap(u, "PENDIENTE"))
                .collect(Collectors.toList());

        return ResponseEntity.ok(pending);
    }

    @PostMapping("/{id}/role")
    public ResponseEntity<?> assignRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Object> req) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();

        UserEntity user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        String roleRaw = req == null ? null : (String) req.get("role");
        if (roleRaw == null || roleRaw.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Role is required"));
        }

        String role = roleRaw.trim().toUpperCase(Locale.ROOT);

        if ("ADMINISTRADOR".equals(role)) {
            if (!adminRepository.existsByIdUser(id)) {
                AdminEntity admin = new AdminEntity();
                admin.setIdUser(id);
                adminRepository.save(admin);
            }
            auditLog.info("ACTION=ASSIGN_ROLE adminId={} targetUserId={} role=ADMINISTRADOR",
                    adminGuard.extractUserId(authHeader), id);
            return ResponseEntity.ok(Map.of("message", "Role assigned", "userId", id, "role", "ADMINISTRADOR"));
        }

        if ("COORDINADOR".equals(role)) {
            Optional<Campaign> campaign = campaignRepository.findAll().stream().findFirst();
            if (campaign.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "No campaigns available to assign this role."
                ));
            }

            Integer campaignId = campaign.get().getId();
            if (!coordinatorRepository.existsByIdIdUserAndIdIdCampaign(id, campaignId)) {
                CoordinatorId coordinatorId = new CoordinatorId();
                coordinatorId.setIdUser(id);
                coordinatorId.setIdCampaign(campaignId);

                Coordinator coordinator = new Coordinator();
                coordinator.setId(coordinatorId);
                coordinator.setIdUser(user);
                coordinator.setIdCampaign(campaign.get());
                coordinatorRepository.save(coordinator);
            }

            auditLog.info("ACTION=ASSIGN_ROLE adminId={} targetUserId={} role=COORDINADOR campaignId={}",
                    adminGuard.extractUserId(authHeader), id, campaignId);
            return ResponseEntity.ok(Map.of("message", "Role assigned", "userId", id, "role", "COORDINADOR"));
        }

        if ("CAPITAN".equals(role) || "CAPITÁN".equals(role)) {
            Optional<Campaign> campaign = campaignRepository.findAll().stream().findFirst();
            if (campaign.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "No campaigns available to assign this role."
                ));
            }

            Integer campaignId = campaign.get().getId();
            if (!captainRepository.existsByIdIdUserAndIdIdCampaign(id, campaignId)) {
                CaptainId captainId = new CaptainId();
                captainId.setIdUser(id);
                captainId.setIdCampaign(campaignId);

                Captain captain = new Captain();
                captain.setId(captainId);
                captain.setIdUser(user);
                captain.setIdCampaign(campaign.get());
                captainRepository.save(captain);
            }

            auditLog.info("ACTION=ASSIGN_ROLE adminId={} targetUserId={} role=CAPITAN campaignId={}",
                    adminGuard.extractUserId(authHeader), id, campaignId);
            return ResponseEntity.ok(Map.of("message", "Role assigned", "userId", id, "role", "CAPITAN"));
        }

        if ("COLABORADOR".equals(role)) {
            Optional<PartnerEntity> partnerEntity = partnerEntityRepository.findAll().stream().findFirst();
            if (partnerEntity.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "No partner entities available to assign this role."
                ));
            }

            if (!partnerEntityManagerRepository.existsById(id)) {
                PartnerEntityManager manager = new PartnerEntityManager();
                manager.setId(id);
                manager.setUserAccounts(user);
                manager.setIdPartnerEntity(partnerEntity.get());
                partnerEntityManagerRepository.save(manager);
            }

            auditLog.info("ACTION=ASSIGN_ROLE adminId={} targetUserId={} role=COLABORADOR partnerEntityId={}",
                    adminGuard.extractUserId(authHeader), id, partnerEntity.get().getId());
            return ResponseEntity.ok(Map.of("message", "Role assigned", "userId", id, "role", "COLABORADOR"));
        }

        if ("RESPONSABLE_TIENDA".equals(role)) {
            Optional<Store> store = storeRepository.findAllByOrderByIdAsc()
                    .stream()
                    .filter(s -> s.getIdResponsible() == null)
                    .findFirst();

            if (store.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "No stores without a responsible user are available."
                ));
            }

            Store target = store.get();
            target.setIdResponsible(user);
            storeRepository.save(target);

            auditLog.info("ACTION=ASSIGN_ROLE adminId={} targetUserId={} role=RESPONSABLE_TIENDA storeId={}",
                    adminGuard.extractUserId(authHeader), id, target.getId());
            return ResponseEntity.ok(Map.of("message", "Role assigned", "userId", id, "role", "RESPONSABLE_TIENDA"));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "message", "Unsupported role requested"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        Integer adminId = adminGuard.extractUserId(authHeader);
        if (id.equals(adminId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot delete your own account"));
        }

        userRepository.deleteById(id);
        auditLog.info("ACTION=DELETE_USER adminId={} targetUserId={}", adminId, id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    private String resolveRole(Integer userId) {
        if (userRepository.isAdmin(userId))                       return "ADMINISTRADOR";
        if (userRepository.isCoordinator(userId))                 return "COORDINADOR";
        if (userRepository.isCaptain(userId))                     return "CAPITÁN";
        if (userRepository.isPartnerEntityManager(userId))        return "COLABORADOR";
        if (storeRepository.existsByIdResponsible_IdUser(userId)) return "RESPONSABLE_TIENDA";
        return "PENDIENTE";
    }

    private static Map<String, Object> toMap(UserEntity u, String role) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",    u.getIdUser());
        m.put("name",  u.getName());
        m.put("email", u.getEmail());
        m.put("phone", u.getPhone());
        m.put("role",  role);
        return m;
    }

    private static ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Access restricted to administrators"));
    }
}