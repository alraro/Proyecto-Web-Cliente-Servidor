/*
*
* Autores:
*	- Hugo Herrero González: 95%
*  - IA Generativa: 5%
*/
package es.grupo8.backend.mapper;

import org.springframework.stereotype.Component;
import es.grupo8.backend.dto.CoverageItemDTO;

// Convertirmos los resultados en DTO, se usa tanto en el dashboard de cobertura por cadena, localidad y zona
@Component
public class CoverageItemDashboardMapper extends MapperDTO<CoverageItemDTO, Object[]>{

    @Override
    public CoverageItemDTO toDTO(Object[] row) {
        if(row == null) return null;

        CoverageItemDTO dto = new CoverageItemDTO();

        dto.setLabel((String) row[0]);
        dto.setTotalStores(((Number) row[1]).longValue());
        dto.setStoresInCampaign(((Number) row[2]).longValue());
        return dto;
    }

}
