/**
 * Controlador REST para gestionar usuarios y sus roles.
 *
 * Autores:
 * - Alejandra Ortiz: 65%
 * - Fernando Luis Pinilla Molina: 5%
 * - Alfonso Ramos Rojas: 5%
 * - IA Generativa: 25%
 */
package es.grupo8.backend.controllers.rest;

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
import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.mapper.UserMapper;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.UserService;

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
    @Autowired private AuthService authService;
    @Autowired private UserService userService;
    @Autowired private UserMapper userMapper;

    private void checkAdmin(String authHeader) {
        Integer userId = authService.extractUserIdFromToken(authHeader);
        if (userId == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
        }
        if (!userService.isAdmin(userId)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "No tienes permiso");
        }
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        checkAdmin(authHeader);
        List<UserDTO> users = userMapper.toDTOList(userRepository.findAll());
        auditLog.info("ACTION=LIST_USERS adminId={} total={}",
                authService.extractUserIdFromToken(authHeader), users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<UserDTO>> getPendingUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        checkAdmin(authHeader);
        List<UserDTO> pending = userRepository.findAll().stream()
                .filter(u -> "PENDIENTE".equals(userMapper.resolveRole(u.getIdUser())))
                .map(userMapper::toDTO)
                .toList();
        auditLog.info("ACTION=LIST_PENDING_USERS adminId={} total={}",
                authService.extractUserIdFromToken(authHeader), pending.size());
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/{id}/role")
    public ResponseEntity<Map<String, Object>> assignRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Object> req) {

        checkAdmin(authHeader);

        UserEntity user = userRepository.findById(id).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Usuario no encontrado"));

        String roleRaw = req == null ? null : (String) req.get("role");
        if (roleRaw == null || roleRaw.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "El rol es obligatorio"));

        String role = roleRaw.trim().toUpperCase(Locale.ROOT);

        // Eliminar todos los roles existentes antes de asignar el nuevo
        // Administrador
        adminRepository.findById(id).ifPresent(adminRepository::delete);

        // Coordinador - eliminar todas las asignaciones de campañas de este usuario
        coordinatorRepository.findAll().stream()
            .filter(c -> c.getId().getIdUser().equals(id))
            .forEach(c -> coordinatorRepository.deleteByIdIdUserAndIdIdCampaign(
                c.getId().getIdUser(), c.getId().getIdCampaign()));

        // Capitán - eliminar todas las asignaciones de campañas de este usuario
        captainRepository.findAll().stream()
            .filter(c -> c.getId().getIdUser().equals(id))
            .forEach(c -> captainRepository.deleteByIdIdUserAndIdIdCampaign(
                c.getId().getIdUser(), c.getId().getIdCampaign()));

        // Responsable de entidad colaboradora
        partnerEntityManagerRepository.findById(id).ifPresent(partnerEntityManagerRepository::delete);

        // Responsable de tienda - quitar el responsable sin eliminar la tienda
        storeRepository.findByIdResponsible_IdUser(id).ifPresent(store -> {
            store.setIdResponsible(null);
            storeRepository.save(store);
        });

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
                authService.extractUserIdFromToken(authHeader), id, role);
        return ResponseEntity.ok(Map.of("message", "Rol asignado", "userId", id, "role", role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        checkAdmin(authHeader);
        if (!userRepository.existsById(id))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Usuario no encontrado"));

        Integer adminId = authService.extractUserIdFromToken(authHeader);
        if (id.equals(adminId))
            return ResponseEntity.badRequest().body(Map.of("message", "No puedes eliminar tu propia cuenta"));

        userRepository.deleteById(id);
        auditLog.info("ACTION=DELETE_USER adminId={} targetUserId={}", adminId, id);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente"));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(AuthException e) {
        return ResponseEntity.status(e.getStatus())
                .body(Map.of("message", e.getMessage()));
    }
}