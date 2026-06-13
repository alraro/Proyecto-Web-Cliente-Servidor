/**
 * Mapeador entre la entidad Incident y su DTO.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.IncidentDTO;
import es.grupo8.backend.entity.Incident;
import org.springframework.stereotype.Component;

@Component
public class IncidentMapper extends MapperDTO<IncidentDTO, Incident> {

    @Override
    public IncidentDTO toDTO(Incident i) {
        if (i == null) return null;
        IncidentDTO dto = new IncidentDTO();
        dto.setId(i.getId());
        dto.setDescription(i.getDescription());
        dto.setCreatedAt(i.getCreatedAt() != null ? i.getCreatedAt().toString() : null);
        dto.setCampaignName(i.getIdCampaign() != null ? i.getIdCampaign().getName() : null);
        dto.setStoreName(i.getIdStore() != null ? i.getIdStore().getName() : null);
        return dto;
    }
}
