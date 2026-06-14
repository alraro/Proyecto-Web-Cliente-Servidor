/**
 * Autores:
 * - Alfonso Ramos Rojas: 100%
 */
package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.VoluntarioResponseDto;
import es.grupo8.backend.dto.VoluntarioResponseDto.CampaignInfo;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.Volunteer;
import es.grupo8.backend.entity.VolunteerShift;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class VolunteerMapper extends MapperDTO<VoluntarioResponseDto, Volunteer> {

    @Override
    public VoluntarioResponseDto toDTO(Volunteer volunteer) {
        if (volunteer == null) return null;

        List<CampaignInfo> campaigns = new ArrayList<>();
        Set<VolunteerShift> shifts = volunteer.getVolunteerShifts();
        if (shifts != null) {
            Set<Integer> addedCampaignIds = new HashSet<>();
            for (VolunteerShift shift : shifts) {
                if (shift.getCampaignStores() != null &&
                    shift.getCampaignStores().getIdCampaign() != null) {

                    Campaign campaign = shift.getCampaignStores().getIdCampaign();
                    if (addedCampaignIds.add(campaign.getId())) {
                        campaigns.add(new CampaignInfo(campaign.getId(), campaign.getName()));
                    }
                }
            }
        }

        Integer entityId = null;
        String entityName = null;
        if (volunteer.getIdPartnerEntity() != null) {
            entityId = volunteer.getIdPartnerEntity().getId();
            entityName = volunteer.getIdPartnerEntity().getName();
        }

        return new VoluntarioResponseDto(
                volunteer.getId(),
                volunteer.getName(),
                volunteer.getPhone(),
                volunteer.getEmail(),
                volunteer.getAddress(),
                entityId,
                entityName,
                campaigns
        );
    }
}
