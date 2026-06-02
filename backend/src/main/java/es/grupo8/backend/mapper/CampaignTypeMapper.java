package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.CampaignTypeResponseDto;
import es.grupo8.backend.entity.CampaignType;
import org.springframework.stereotype.Component;

@Component
public class CampaignTypeMapper extends MapperDTO<CampaignTypeResponseDto, CampaignType> {

    @Override
    public CampaignTypeResponseDto toDTO(CampaignType entity) {
        if (entity == null) return null;
        CampaignTypeResponseDto dto = new CampaignTypeResponseDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }
}
