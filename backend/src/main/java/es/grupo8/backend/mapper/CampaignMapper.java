package es.grupo8.backend.mapper;
import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.entity.Campaign;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class CampaignMapper extends MapperDTO<CampaignDTO, Campaign> {
    @Override
    public CampaignDTO toDTO(Campaign entity) {
        if (entity == null) return null; 
        CampaignDTO dto = new CampaignDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setIdType(entity.getIdType());
        dto.setStores(entity.getStores()); 
        dto.setCaptains(entity.getCaptains()); 
        dto.setCoordinators(entity.getCoordinators()); 
        dto.setStoresInCampaign(entity.getStores() != null ? entity.getStores().size() : 0);
        return dto;
    }
}
