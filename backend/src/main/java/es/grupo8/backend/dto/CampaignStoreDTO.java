package es.grupo8.backend.dto;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.entity.CampaignStoreId;
import es.grupo8.backend.entity.VolunteerShift;
import java.util.Set;

import lombok.Data;

@Data
public class CampaignStoreDTO {
    private CampaignStoreId id;
    private Campaign idCampaign;
    private Store idStore;
    private Set<VolunteerShift> volunteerShifts;
}
