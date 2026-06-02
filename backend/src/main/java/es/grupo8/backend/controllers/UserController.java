/**
 * Controlador REST para gestionar usuarios.
 *
 * Autores:
 * - Alfonso Ramos: 40%
 * - Alejandra Ortiz: 40%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.grupo8.backend.dao.AdminRepository;
import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dao.PartnerEntityManagerRepository;
import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dto.UserDTO;
import es.grupo8.backend.entity.*;
import es.grupo8.backend.mapper.UserMapper;
import es.grupo8.backend.security.AdminGuard;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    @Autowired private UserRepository userRepository;
    @Autowired private AdminRepository adminRepository;
    @Autowired private CampaignRepository campaignRepository;
    @Autowired private CaptainRepository captainRepository;
    @Autowired private CoordinatorRepository coordinatorRepository;
    @Autowired private PartnerEntityRepository partnerEntityRepository;
    @Autowired private PartnerEntityManagerRepository partnerEntityManagerRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private AdminGuard adminGuard;
    @Autowired private UserMapper userMapper;

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();
        List<UserDTO> users = userMapper.toDTOList(userRepository.findAll());
        auditLog.info("ACTION=LIST_USERS adminId={} total={}",
                adminGuard.extractUserId(authHeader), users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();
        List<UserDTO> pending = userRepository.findAll().stream()
                .filter(u -> "PENDIENTE".equals(userMapper.resolveRole(u.getIdUser())))
                .map(userMapper::toDTO)
                .toList();
        auditLog.info("ACTION=LIST_PENDING_USERS adminId={} total={}",
                adminGuard.extractUserId(authHeader), pending.size());
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/{id}/role")
    public ResponseEntity<?> assignRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Object> req) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();

        UserEntity user = userRepository.findById(id).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Usuario no encontrado"));

        String roleRaw = req == null ? null : (String) req.get("role");
        if (roleRaw == null || roleRaw.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "El rol es obligatorio"));

        String role = roleRaw.trim().toUpperCase(Locale.ROOT);

        switch (role) {
            case "ADMINISTRADOR" -> {
                if (!adminRepository.existsByIdUser(id)) {
                    AdminEntity admin = new AdminEntity();
                    admin.setIdUser(id);
                    adminRepository.save(admin);
                }
            }
            case "COORDINADOR" -> {
                Optional<Campaign> campaign = campaignRepository.findAll().stream().findFirst();
                if (campaign.isEmpty())
                    return ResponseEntity.badRequest().body(Map.of("message", "No hay campañas disponibles para asignar este rol."));
                Integer cid = campaign.get().getId();
                if (!coordinatorRepository.existsByIdIdUserAndIdIdCampaign(id, cid)) {
                    CoordinatorId coordinatorId = new CoordinatorId();
                    coordinatorId.setIdUser(id); coordinatorId.setIdCampaign(cid);
                    Coordinator coordinator = new Coordinator();
                    coordinator.setId(coordinatorId); coordinator.setIdUser(user); coordinator.setIdCampaign(campaign.get());
                    coordinatorRepository.save(coordinator);
                }
            }
            case "CAPITAN", "CAPITÁN" -> {
                Optional<Campaign> campaign = campaignRepository.findAll().stream().findFirst();
                if (campaign.isEmpty())
                    return ResponseEntity.badRequest().body(Map.of("message", "No hay campañas disponibles para asignar este rol."));
                Integer cid = campaign.get().getId();
                if (!captainRepository.existsByIdIdUserAndIdIdCampaign(id, cid)) {
                    CaptainId captainId = new CaptainId();
                    captainId.setIdUser(id); captainId.setIdCampaign(cid);
                    Captain captain = new Captain();
                    captain.setId(captainId); captain.setIdUser(user); captain.setIdCampaign(campaign.get());
                    captainRepository.save(captain);
                }
            }
            case "COLABORADOR" -> {
                Optional<PartnerEntity> pe = partnerEntityRepository.findAll().stream().findFirst();
                if (pe.isEmpty())
                    return ResponseEntity.badRequest().body(Map.of("message", "No hay entidades colaboradoras disponibles para asignar este rol."));
                if (!partnerEntityManagerRepository.existsById(id)) {
                    PartnerEntityManager manager = new PartnerEntityManager();
                    manager.setUserAccounts(user); manager.setIdPartnerEntity(pe.get());
                    partnerEntityManagerRepository.save(manager);
                }
            }
            case "RESPONSABLE_TIENDA" -> {
                Optional<Store> store = storeRepository.findAllByOrderByIdAsc().stream()
                        .filter(s -> s.getIdResponsible() == null).findFirst();
                if (store.isEmpty())
                    return ResponseEntity.badRequest().body(Map.of("message", "No hay tiendas sin responsable disponibles."));
                store.get().setIdResponsible(user);
                storeRepository.save(store.get());
            }
            default -> { return ResponseEntity.badRequest().body(Map.of("message", "Rol no soportado: " + role)); }
        }

        auditLog.info("ACTION=ASSIGN_ROLE adminId={} targetUserId={} role={}",
                adminGuard.extractUserId(authHeader), id, role);
        return ResponseEntity.ok(Map.of("message", "Rol asignado", "userId", id, "role", role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();
        if (!userRepository.existsById(id))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Usuario no encontrado"));

        Integer adminId = adminGuard.extractUserId(authHeader);
        if (id.equals(adminId))
            return ResponseEntity.badRequest().body(Map.of("message", "No puedes eliminar tu propia cuenta"));

        userRepository.deleteById(id);
        auditLog.info("ACTION=DELETE_USER adminId={} targetUserId={}", adminId, id);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente"));
    }

    private static ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Acceso restringido a administradores"));
    }
}