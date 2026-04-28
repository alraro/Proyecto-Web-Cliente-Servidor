package es.grupo8.backend.controllers;

import java.time.Instant;
import java.util.Comparator;
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
import es.grupo8.backend.dao.PostalCodeRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dto.StoreRequestDto;
import es.grupo8.backend.dto.StoreResponseDto;
import es.grupo8.backend.entity.ChainEntity;
import es.grupo8.backend.entity.Locality;
import es.grupo8.backend.entity.PostalCode;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.security.AdminGuard;
import es.grupo8.backend.security.CoordinatorGuard;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    @Autowired private StoreRepository      storeRepository;
    @Autowired private ChainRepository      chainRepository;
    @Autowired private PostalCodeRepository postalCodeRepository;
    @Autowired private AdminGuard           adminGuard;
    @Autowired private CoordinatorGuard     coordinatorGuard;

    @GetMapping
    public ResponseEntity<?> getStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer localityId,
            @RequestParam(required = false) Integer zoneId) {

        if (!adminGuard.isAdmin(authHeader) && !coordinatorGuard.isCoordinator(authHeader)) {
            return forbidden();
        }

        List<Store> stores = storeRepository.findAllByOrderByIdAsc();

        if (chainId != null) {
            stores = stores.stream()
                    .filter(s -> s.getIdChain() != null && chainId.equals(s.getIdChain().getIdChain()))
                    .collect(Collectors.toList());
        }
        if (localityId != null) {
            stores = stores.stream()
                    .filter(s -> s.getPostalCode() != null
                            && s.getPostalCode().getIdLocality() != null
                            && localityId.equals(s.getPostalCode().getIdLocality().getId()))
                    .collect(Collectors.toList());
        }
        if (zoneId != null) {
            stores = stores.stream()
                    .filter(s -> s.getPostalCode() != null
                            && s.getPostalCode().getIdLocality() != null
                            && s.getPostalCode().getIdLocality().getIdZone() != null
                            && zoneId.equals(s.getPostalCode().getIdLocality().getIdZone().getId()))
                    .collect(Collectors.toList());
        }

        List<StoreResponseDto> result = stores.stream()
            .sorted(Comparator.comparing(Store::getId, Comparator.nullsLast(Integer::compareTo)))
                .map(StoreController::toDto)
                .collect(Collectors.toList());

        auditLog.info("ACTION=LIST_STORES userId={} timestamp={} filters=[chainId={},localityId={},zoneId={}] count={}",
                adminGuard.extractUserId(authHeader), Instant.now(), chainId, localityId, zoneId, result.size());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();

        Store store = storeRepository.findById(id).orElse(null);
        if (store == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Store not found"));
        }

        auditLog.info("ACTION=GET_STORE userId={} timestamp={} storeId={}",
                adminGuard.extractUserId(authHeader), Instant.now(), id);

        return ResponseEntity.ok(toDto(store));
    }

    @PostMapping
    public ResponseEntity<?> createStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) StoreRequestDto req) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();

        ValidationResult v = validate(req);
        if (v.error != null) return ResponseEntity.badRequest().body(Map.of("message", v.error));

        Store store = new Store();
        store.setName(v.name);
        store.setAddress(v.address);
        store.setPostalCode(v.postalCode);
        store.setIdChain(v.chain);

        Store saved = storeRepository.save(store);

        auditLog.info("ACTION=CREATE_STORE userId={} timestamp={} storeId={} name='{}' postalCode={} chainId={}",
                adminGuard.extractUserId(authHeader), Instant.now(),
                saved.getId(), saved.getName(),
                saved.getPostalCode() != null ? saved.getPostalCode().getPostalCode() : null,
                saved.getIdChain() != null ? saved.getIdChain().getIdChain() : null);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) StoreRequestDto req) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();

        Store store = storeRepository.findById(id).orElse(null);
        if (store == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Store not found"));
        }

        ValidationResult v = validate(req);
        if (v.error != null) return ResponseEntity.badRequest().body(Map.of("message", v.error));

        store.setName(v.name);
        store.setAddress(v.address);
        store.setPostalCode(v.postalCode);
        store.setIdChain(v.chain);

        Store updated = storeRepository.save(store);

        auditLog.info("ACTION=UPDATE_STORE userId={} timestamp={} storeId={} name='{}' postalCode={} chainId={}",
                adminGuard.extractUserId(authHeader), Instant.now(),
                updated.getId(), updated.getName(),
                updated.getPostalCode() != null ? updated.getPostalCode().getPostalCode() : null,
                updated.getIdChain() != null ? updated.getIdChain().getIdChain() : null);

        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();

        if (!storeRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Store not found"));
        }

        storeRepository.deleteById(id);

        auditLog.info("ACTION=DELETE_STORE userId={} timestamp={} storeId={}",
                adminGuard.extractUserId(authHeader), Instant.now(), id);

        return ResponseEntity.ok(Map.of("message", "Store deleted successfully"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Access restricted to administrators"));
    }

    static StoreResponseDto toDto(Store s) {
        String postalCode = null, locality = null, zone = null;
        Integer localityId = null, zoneId = null, chainId = null;
        String chainName = null;

        if (s.getPostalCode() != null) {
            PostalCode pc = s.getPostalCode();
            postalCode = pc.getPostalCode();
            if (pc.getIdLocality() != null) {
                Locality loc = pc.getIdLocality();
                locality   = loc.getName();
                localityId = loc.getId();
                if (loc.getIdZone() != null) {
                    zone   = loc.getIdZone().getName();
                    zoneId = loc.getIdZone().getId();
                }
            }
        }
        if (s.getIdChain() != null) {
            chainId   = s.getIdChain().getIdChain();
            chainName = s.getIdChain().getName();
        }

        return new StoreResponseDto(
                s.getId(), s.getName(), s.getAddress(),
                postalCode, locality, localityId, zone, zoneId,
                chainId, chainName
        );
    }

    private ValidationResult validate(StoreRequestDto req) {
        ValidationResult r = new ValidationResult();

        String name    = trimToNull(req == null ? null : req.getName());
        String address = trimToNull(req == null ? null : req.getAddress());
        String cp      = trimToNull(req == null ? null : req.getPostalCode());
        Integer chainIdRaw = req == null ? null : req.getChainId();

        if (name == null)             { r.error = "Name is required"; return r; }
        if (name.length() > 255)      { r.error = "Name cannot exceed 255 characters"; return r; }
        if (address != null && address.length() > 500) { r.error = "Address cannot exceed 500 characters"; return r; }

        PostalCode postalCode = null;
        if (cp != null) {
            if (!cp.matches("^[0-9]{5}$")) { r.error = "Postal code must be exactly 5 digits"; return r; }
            postalCode = postalCodeRepository.findById(cp).orElse(null);
            if (postalCode == null)         { r.error = "Postal code not found"; return r; }
        }

        ChainEntity chain = null;
        if (chainIdRaw != null) {
            chain = chainRepository.findById(chainIdRaw).orElse(null);
            if (chain == null) { r.error = "Chain not found"; return r; }
        }

        r.name = name; r.address = address; r.postalCode = postalCode; r.chain = chain;
        return r;
    }

    private static class ValidationResult {
        String error, name, address;
        PostalCode  postalCode;
        ChainEntity chain;
    }

    private static String trimToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}