package es.grupo8.backend.controllers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.entity.CampaignStore;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.entity.VolunteerShift;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/stores")
public class ResponsibleStoreController {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    @Autowired private StoreRepository         storeRepository;
    @Autowired private CampaignStoreRepository campaignStoreRepository;

    @Value("${app.jwt.secret:change-this-secret-in-production-change-this-secret-in-production}")
    private String jwtSecret;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = buildKey(jwtSecret);
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<?> getStoreDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        // 1. Extraer userId del token JWT
        Integer userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        // 2. Cargar la tienda
        Store store = storeRepository.findById(id).orElse(null);
        if (store == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Store not found"));
        }

        // 3. Verificar que el usuario es el Responsable de ESTA tienda
        boolean isResponsible = store.getIdResponsible() != null
                && userId.equals(store.getIdResponsible().getIdUser());

        if (!isResponsible) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access restricted to the assigned store manager"));
        }

        // 4. Construir respuesta con datos de tienda + turnos
        Map<String, Object> response = buildDetailResponse(store);

        auditLog.info("ACTION=VIEW_STORE_DETAIL userId={} storeId={}", userId, id);

        return ResponseEntity.ok(response);
    }

    // Helpers 
    private Map<String, Object> buildDetailResponse(Store store) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",      store.getId());
        m.put("name",    store.getName());
        m.put("address", store.getAddress());

        if (store.getPostalCode() != null) {
            m.put("postalCode", store.getPostalCode().getPostalCode());
            if (store.getPostalCode().getIdLocality() != null) {
                m.put("locality", store.getPostalCode().getIdLocality().getName());
                if (store.getPostalCode().getIdLocality().getIdZone() != null) {
                    m.put("zone", store.getPostalCode().getIdLocality().getIdZone().getName());
                }
            }
        }

        if (store.getIdChain() != null) {
            m.put("chainId",   store.getIdChain().getIdChain());
            m.put("chainName", store.getIdChain().getName());
        }

        // Turnos programados — recorremos CampaignStore vinculados a esta tienda
        List<Map<String, Object>> shifts = buildShifts(store);
        m.put("scheduledShifts", shifts);

        return m;
    }

    private List<Map<String, Object>> buildShifts(Store store) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<CampaignStore> campaignStores = campaignStoreRepository.findAll()
                .stream()
                .filter(cs -> cs.getIdStore() != null
                        && store.getId().equals(cs.getIdStore().getId()))
                .collect(Collectors.toList());

        for (CampaignStore cs : campaignStores) {
            for (VolunteerShift vs : cs.getVolunteerShifts()) {
                Map<String, Object> shift = new LinkedHashMap<>();
                shift.put("endTime",    vs.getEndTime() != null ? vs.getEndTime().toString() : null);
                shift.put("attendance", vs.getAttendance());
                shift.put("notes",      vs.getNotes());

                /* Tiene que hacerse primero Volunteer */
                /*if (cs.getIdCampaign() != null) {
                    shift.put("campaignId",   cs.getIdCampaign().getIdCampaign());
                    shift.put("campaignName", cs.getIdCampaign().getName());
                }
                if (vs.getIdVolunteer() != null && vs.getIdVolunteer().getIdUser() != null) {
                    shift.put("volunteerId",   vs.getIdVolunteer().getIdUser().getIdUser());
                    shift.put("volunteerName", vs.getIdVolunteer().getIdUser().getName());
                }*/

                result.add(shift);
            }
        }

        return result;
    }

    private Integer extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            String subject = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(authHeader.substring(7).trim())
                    .getPayload()
                    .getSubject();
            return subject != null ? Integer.valueOf(subject) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static SecretKey buildKey(String secret) {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (RuntimeException ignored) {
            try {
                byte[] hash = MessageDigest.getInstance("SHA-256")
                        .digest(secret.getBytes(StandardCharsets.UTF_8));
                return Keys.hmacShaKeyFor(hash);
            } catch (NoSuchAlgorithmException ex) {
                throw new IllegalStateException("Failed to initialize JWT key", ex);
            }
        }
    }
}