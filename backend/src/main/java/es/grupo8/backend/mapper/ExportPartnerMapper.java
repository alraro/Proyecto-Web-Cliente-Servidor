/**
 * Mapeador para la exportación de entidades colaboradoras.
 *
 * Autores:
 * - Hugo Herrero González: 95%
 * - Fernando Luis Pinilla Molina: 5%
 */
package es.grupo8.backend.mapper;

import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.ExportPartnerDTO;
import es.grupo8.backend.entity.PartnerEntity;

@Component
public class ExportPartnerMapper extends MapperDTO<ExportPartnerDTO, PartnerEntity> {

    @Override
    public ExportPartnerDTO toDTO(PartnerEntity p) {
        if(p == null) return null;

        ExportPartnerDTO dto = new ExportPartnerDTO();

        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setAddress(nullSafe(p.getAddress()));
        dto.setPhone(nullSafe(p.getPhone()));

        return dto;
    }

    private String nullSafe(String v) { return v != null ? v : ""; }


}