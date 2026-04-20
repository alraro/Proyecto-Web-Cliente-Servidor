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

    // GET /api/chains
    @GetMapping
    public ResponseEntity<?> getCadenas(
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

    // POST /api/chains
    @PostMapping
    public ResponseEntity<?> createCadena(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!adminGuard.isAdmin(authHeader)) {
            return forbidden();
        }

        String nombre = trimToNull(request == null ? null : (String) request.get("nombre"));
        String codigo = trimToNull(request == null ? null : (String) request.get("codigo"));
        Object participacionRaw = request == null ? null : request.get("participacion");

        if (nombre == null || codigo == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Nombre y codigo son obligatorios"));
        }
        if (!isValidCodigo(codigo)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "El codigo solo puede contener letras, numeros, guiones y guiones bajos (max 50 caracteres)"));
        }
        if (nombre.length() > 255) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "El nombre no puede superar 255 caracteres"));
        }
        if (chainRepository.existsByCodigo(codigo)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Ya existe una cadena con ese codigo"));
        }

        ChainEntity chain = new ChainEntity();
        chain.setNombre(nombre);
        chain.setCodigo(codigo);
        chain.setParticipacion(participacionRaw instanceof Boolean b ? b : false);

        ChainEntity saved = chainRepository.save(chain);

        auditLog.info("ACTION=CREATE_CHAIN userId={} timestamp={} cadenaId={} nombre='{}' codigo='{}' participacion={}",
                adminGuard.extractUserId(authHeader), Instant.now(),
                saved.getIdChain(), saved.getNombre(), saved.getCodigo(), saved.getParticipacion());

        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(saved,
                "message", "Cadena creada correctamente"));
    }

    // PUT /api/chains/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCadena(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!adminGuard.isAdmin(authHeader)) {
            return forbidden();
        }

        ChainEntity chain = chainRepository.findById(id).orElse(null);
        if (chain == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Cadena no encontrada"));
        }

        String nombre = trimToNull(request == null ? null : (String) request.get("nombre"));
        String codigo = trimToNull(request == null ? null : (String) request.get("codigo"));
        Object participacionRaw = request == null ? null : request.get("participacion");

        if (nombre == null || codigo == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Nombre y codigo son obligatorios"));
        }
        if (!isValidCodigo(codigo)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "El codigo solo puede contener letras, numeros, guiones y guiones bajos (max 50 caracteres)"));
        }
        if (nombre.length() > 255) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "El nombre no puede superar 255 caracteres"));
        }
        if (!codigo.equals(chain.getCodigo()) && chainRepository.existsByCodigo(codigo)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Ya existe una cadena con ese codigo"));
        }

        chain.setNombre(nombre);
        chain.setCodigo(codigo);
        chain.setParticipacion(participacionRaw instanceof Boolean b ? b : chain.getParticipacion());

        ChainEntity updated = chainRepository.save(chain);

        auditLog.info("ACTION=UPDATE_CHAIN userId={} timestamp={} cadenaId={} nombre='{}' codigo='{}' participacion={}",
                adminGuard.extractUserId(authHeader), Instant.now(),
                updated.getIdChain(), updated.getNombre(), updated.getCodigo(), updated.getParticipacion());

        return ResponseEntity.ok(toMap(updated, "message", "Cadena actualizada correctamente"));
    }

    // DELETE /api/chains/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCadena(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!adminGuard.isAdmin(authHeader)) {
            return forbidden();
        }
        if (!chainRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Cadena no encontrada"));
        }

        chainRepository.deleteById(id);

        auditLog.info("ACTION=DELETE_CHAIN userId={} timestamp={} cadenaId={}",
                adminGuard.extractUserId(authHeader), Instant.now(), id);

        return ResponseEntity.ok(Map.of("message", "Cadena eliminada correctamente"));
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private static ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Acceso restringido a administradores"));
    }

    private static Map<String, Object> toMap(ChainEntity c) {
        return Map.of(
                "id",            c.getIdChain(),
                "nombre",        c.getNombre(),
                "codigo",        c.getCodigo(),
                "participacion", c.getParticipacion()
        );
    }

    private static Map<String, Object> toMap(ChainEntity c, String extraKey, Object extraVal) {
        return Map.of(
                "id",            c.getIdChain(),
                "nombre",        c.getNombre(),
                "codigo",        c.getCodigo(),
                "participacion", c.getParticipacion(),
                extraKey,        extraVal
        );
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isValidCodigo(String codigo) {
        return codigo != null
                && codigo.length() <= 50
                && codigo.matches("^[A-Za-z0-9_\\-]+$");
    }
}