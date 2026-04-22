package es.grupo8.backend.controllers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dao.ChainRepository;
import es.grupo8.backend.entity.ChainEntity;
import es.grupo8.backend.security.AdminGuard;

@RestController
@RequestMapping("/api/chains")
public class ChainController {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    @Autowired
    private ChainRepository chainRepository;

    @Autowired
    private AdminGuard adminGuard;


    @GetMapping
    public ResponseEntity<?> getChains(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!adminGuard.isAdmin(authHeader)) {
            return forbidden();
        }

        List<Map<String, Object>> chains = chainRepository.findAll()
                .stream()
                .map(ChainController::toMap)
                .collect(Collectors.toList());

        auditLog.info("ACTION=LIST_CHAINS userId={} timestamp={} count={}",
                adminGuard.extractUserId(authHeader), Instant.now(), chains.size());

        return ResponseEntity.ok(chains);
    }


    @PostMapping
    public ResponseEntity<?> createChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!adminGuard.isAdmin(authHeader)) {
            return forbidden();
        }

        String name = trimToNull(request == null ? null : (String) request.get("name"));
        String code = trimToNull(request == null ? null : (String) request.get("code"));
        Object participationRaw = request == null ? null : request.get("participation");

        if (name == null || code == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Name and code are required"));
        }
        if (!isValidCode(code)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Code can only contain letters, numbers, hyphens, and underscores (max 50 characters)"));
        }
        if (name.length() > 255) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Name cannot exceed 255 characters"));
        }
        if (chainRepository.existsByCode(code)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "A chain with that code already exists"));
        }

        ChainEntity chain = new ChainEntity();
        chain.setName(name);
        chain.setCode(code);
        chain.setParticipation(participationRaw instanceof Boolean b ? b : false);

        ChainEntity saved = chainRepository.save(chain);

        auditLog.info("ACTION=CREATE_CHAIN userId={} timestamp={} chainId={} name='{}' code='{}' participation={}",
                adminGuard.extractUserId(authHeader), Instant.now(),
                saved.getIdChain(), saved.getName(), saved.getCode(), saved.getParticipation());

        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(saved,
                "message", "Chain created successfully"));
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!adminGuard.isAdmin(authHeader)) {
            return forbidden();
        }

        ChainEntity chain = chainRepository.findById(id).orElse(null);
        if (chain == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Chain not found"));
        }

        String name = trimToNull(request == null ? null : (String) request.get("name"));
        String code = trimToNull(request == null ? null : (String) request.get("code"));
        Object participationRaw = request == null ? null : request.get("participation");

        if (name == null || code == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Name and code are required"));
        }
        if (!isValidCode(code)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Code can only contain letters, numbers, hyphens, and underscores (max 50 characters)"));
        }
        if (name.length() > 255) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Name cannot exceed 255 characters"));
        }
        if (!code.equals(chain.getCode()) && chainRepository.existsByCode(code)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "A chain with that code already exists"));
        }

        chain.setName(name);
        chain.setCode(code);
        chain.setParticipation(participationRaw instanceof Boolean b ? b : chain.getParticipation());

        ChainEntity updated = chainRepository.save(chain);

        auditLog.info("ACTION=UPDATE_CHAIN userId={} timestamp={} chainId={} name='{}' code='{}' participation={}",
                adminGuard.extractUserId(authHeader), Instant.now(),
                updated.getIdChain(), updated.getName(), updated.getCode(), updated.getParticipation());

        return ResponseEntity.ok(toMap(updated, "message", "Chain updated successfully"));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!adminGuard.isAdmin(authHeader)) {
            return forbidden();
        }
        if (!chainRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Chain not found"));
        }

        chainRepository.deleteById(id);

        auditLog.info("ACTION=DELETE_CHAIN userId={} timestamp={} chainId={}",
                adminGuard.extractUserId(authHeader), Instant.now(), id);

        return ResponseEntity.ok(Map.of("message", "Chain deleted successfully"));
    }



    private static ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Access restricted to administrators"));
    }

    private static Map<String, Object> toMap(ChainEntity c) {
        return Map.of(
                "id",            c.getIdChain(),
                "name",        c.getName(),
                "code",        c.getCode(),
                "participation", c.getParticipation()
        );
    }

    private static Map<String, Object> toMap(ChainEntity c, String extraKey, Object extraVal) {
        return Map.of(
                "id",            c.getIdChain(),
                "name",        c.getName(),
                "code",        c.getCode(),
                "participation", c.getParticipation(),
                extraKey,        extraVal
        );
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isValidCode(String code) {
        return code != null
                && code.length() <= 50
                && code.matches("^[A-Za-z0-9_\\-]+$");
    }
}