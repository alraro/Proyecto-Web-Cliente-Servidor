/**
 * Autores:
 * - Alfonso Ramos Rojas: 100%
 */
package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.entity.PartnerEntity;
import org.springframework.stereotype.Component;

@Component
public class PartnerEntityMapper extends MapperDTO<PartnerEntityResponseDto, PartnerEntity> {

    @Override
    public PartnerEntityResponseDto toDTO(PartnerEntity entity) {
        if (entity == null) return null;

        return new PartnerEntityResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                entity.getPhone()
        );
    }
}
