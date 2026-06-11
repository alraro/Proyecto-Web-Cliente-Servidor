/**
 * Mapeador entre la relación campaña-tienda y su DTO.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 90%
 * - IA Generativa: 10%
 */
package es.grupo8.backend.mapper;

import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.CampaignStoreDTO;
import es.grupo8.backend.entity.CampaignStore;

@Component
public class CampaignStoreMapper extends MapperDTO<CampaignStoreDTO, CampaignStore> {

  @Override
  public CampaignStoreDTO toDTO(CampaignStore entity){
      if (entity == null) return null;
      CampaignStoreDTO campaignStoreDTO = new CampaignStoreDTO();
      campaignStoreDTO.setId(entity.getId());
      campaignStoreDTO.setIdCampaign(entity.getIdCampaign());
      campaignStoreDTO.setIdStore(entity.getIdStore());
      campaignStoreDTO.setVolunteerShifts(entity.getVolunteerShifts()); 
      
      return campaignStoreDTO;
  }
}
