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
import org.springframework.web.bind.annotation.*;

import es.grupo8.backend.dao.ChainRepository;
import es.grupo8.backend.dto.ChainRequestDto;
import es.grupo8.backend.dto.ChainResponseDto;
import es.grupo8.backend.entity.ChainEntity;
import es.grupo8.backend.security.AdminGuard;

@RestController
@RequestMapping("/api/chains")
public class ChainController {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    @Autowired private ChainRepository chainRepository;
    @Autowired private AdminGuard      adminGuard;

    @GetMapping
    public ResponseEntity<?> getChains(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();

        List<ChainResponseDto> chains = chainRepository.findAllByOrderByIdChainAsc()
                .stream()
                .map(ChainController::toDto)
                .collect(Collectors.toList());

        auditLog.info("ACTION=LIST_CHAINS userId={} timestamp={} count={}",
                adminGuard.extractUserId(authHeader), Instant.now(), chains.size());

        return ResponseEntity.ok(chains);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();

        return chainRepository.findById(id)
                .<ResponseEntity<?>>map(c -> ResponseEntity.ok(toDto(c)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Chain not found")));
    }

    @PostMapping
    public ResponseEntity<?> createChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) ChainRequestDto request) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();

        String err = validate(request, null);
        if (err != null) return ResponseEntity.badRequest().body(Map.of("message", err));

        if (chainRepository.existsByCode(request.getCode().trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "A chain with that code already exists"));
        }

        ChainEntity chain = new ChainEntity();
        chain.setName(request.getName().trim());
        chain.setCode(request.getCode().trim());
        chain.setParticipation(request.getParticipation() != null ? request.getParticipation() : false);

        ChainEntity saved = chainRepository.save(chain);

        auditLog.info("ACTION=CREATE_CHAIN userId={} timestamp={} chainId={} name='{}' code='{}' participation={}",
                adminGuard.extractUserId(authHeader), Instant.now(),
                saved.getIdChain(), saved.getName(), saved.getCode(), saved.getParticipation());

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) ChainRequestDto request) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();

        ChainEntity chain = chainRepository.findById(id).orElse(null);
        if (chain == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Chain not found"));
        }

        String err = validate(request, null);
        if (err != null) return ResponseEntity.badRequest().body(Map.of("message", err));

        String newCode = request.getCode().trim();
        if (!newCode.equals(chain.getCode()) && chainRepository.existsByCode(newCode)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "A chain with that code already exists"));
        }

        chain.setName(request.getName().trim());
        chain.setCode(newCode);
        chain.setParticipation(request.getParticipation() != null ? request.getParticipation() : chain.getParticipation());

        ChainEntity updated = chainRepository.save(chain);

        auditLog.info("ACTION=UPDATE_CHAIN userId={} timestamp={} chainId={} name='{}' code='{}' participation={}",
                adminGuard.extractUserId(authHeader), Instant.now(),
                updated.getIdChain(), updated.getName(), updated.getCode(), updated.getParticipation());

        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();

        if (!chainRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Chain not found"));
        }

        chainRepository.deleteById(id);

        auditLog.info("ACTION=DELETE_CHAIN userId={} timestamp={} chainId={}",
                adminGuard.extractUserId(authHeader), Instant.now(), id);

        return ResponseEntity.ok(Map.of("message", "Chain deleted successfully"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Access restricted to administrators"));
    }

    static ChainResponseDto toDto(ChainEntity c) {
        return new ChainResponseDto(c.getIdChain(), c.getName(), c.getCode(), c.getParticipation());
    }

    /** Returns error message or null if valid. */
    private static String validate(ChainRequestDto req, Integer existingId) {
        if (req == null) return "Request body is required";
        String name = req.getName() == null ? "" : req.getName().trim();
        String code = req.getCode() == null ? "" : req.getCode().trim();
        if (name.isEmpty()) return "Name is required";
        if (name.length() > 255) return "Name cannot exceed 255 characters";
        if (code.isEmpty()) return "Code is required";
        if (!code.matches("^[A-Za-z0-9_\\-]+$") || code.length() > 50) {
            return "Code can only contain letters, numbers, hyphens, and underscores (max 50 characters)";
        }
        return null;
    }
}