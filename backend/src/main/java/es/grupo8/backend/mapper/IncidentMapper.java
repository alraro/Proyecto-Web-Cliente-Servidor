/**
 * Mapeador entre la entidad Incident y su DTO.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 90%
 * - IA Generativa: 10%
 */
package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.IncidentDTO;
import es.grupo8.backend.entity.Incident;
import org.springframework.stereotype.Component;

/**
 * Maps {@link Incident} entities to {@link IncidentDTO}.
 */
@Component
public class IncidentMapper extends MapperDTO<IncidentDTO, Incident> {

    @Override
    public IncidentDTO toDTO(Incident i) {
        if (i == null) return null;
        return new IncidentDTO(
                i.getId(),
                i.getDescription(),
                i.getCreatedAt() != null ? i.getCreatedAt().toString() : null,
                i.getIdCampaign() != null ? i.getIdCampaign().getName() : null,
                i.getIdStore()    != null ? i.getIdStore().getName()    : null
        );
    }
}
