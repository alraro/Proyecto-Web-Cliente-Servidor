/**
 * Servicio para obtener el detalle de la tienda del responsable.
 *
 * Autores:
 * - Alejandra Ortiz: 100%
 */
package es.grupo8.backend.services;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.entity.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Busca la tienda, verifica que el usuario es el responsable y construye
 * el mapa con los datos de la tienda + turnos programados.
 */
@Service
@AllArgsConstructor
public class ResponsibleStoreService {

    private final StoreRepository         storeRepository;
    private final CampaignStoreRepository campaignStoreRepository;

    /**
     * Devuelve el detalle de una tienda si el userId es su responsable asignado.
     * Lanza 403 si no es el responsable, 404 si la tienda no existe.
     */
    public Map<String, Object> getStoreDetail(Integer storeId, Integer userId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));

        boolean isResponsible = store.getIdResponsible() != null
                && userId.equals(store.getIdResponsible().getIdUser());

        if (!isResponsible) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access restricted to the assigned store manager");
        }

        return buildDetailResponse(store);
    }

    // Construcción de la respuesta
    private Map<String, Object> buildDetailResponse(Store store) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", store.getId());
        m.put("name", store.getName());
        m.put("address", store.getAddress());

        PostalCode pc = store.getPostalCode();
        if (pc != null) {
            m.put("postalCode", pc.getPostalCode());
            Locality loc = pc.getIdLocality();
            if (loc != null) {
                m.put("locality", loc.getName());
                GeographicZone zone = loc.getIdZone();
                if (zone != null) {
                    m.put("zone", zone.getName());
                }
            }
        }

        if (store.getIdChain() != null) {
            m.put("chainId", store.getIdChain().getIdChain());
            m.put("chainName", store.getIdChain().getName());
        }

        m.put("scheduledShifts", buildShifts(store));
        return m;
    }

    // Construye la lista de turnos de una tienda.
    private List<Map<String, Object>> buildShifts(Store store) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<CampaignStore> campaignStores = campaignStoreRepository.findByIdStore_Id(store.getId());

        for (CampaignStore cs : campaignStores) {
            // Datos de campaña (cuando esté disponible la entidad Volunteer)
            String campaignName = cs.getIdCampaign() != null ? cs.getIdCampaign().getName() : null;

            for (VolunteerShift vs : cs.getVolunteerShifts()) {
                Map<String, Object> shift = new LinkedHashMap<>();

                shift.put("campaignName", campaignName);
                shift.put("endTime",      vs.getEndTime() != null ? vs.getEndTime().toString() : null);
                shift.put("attendance",   vs.getAttendance());
                shift.put("notes",        vs.getNotes());

                if (vs.getIdVolunteer() != null) {
                    shift.put("volunteerName", vs.getIdVolunteer().getName());
                }

                result.add(shift);
            }
        }
        return result;
    }
}