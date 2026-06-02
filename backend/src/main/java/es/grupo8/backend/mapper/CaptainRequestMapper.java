package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.CaptainRequestResponseDto;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.CaptainRequest;
import es.grupo8.backend.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class CaptainRequestMapper extends MapperDTO<CaptainRequestResponseDto, CaptainRequest> {

    private String nullSafe(String v) { return v != null ? v : ""; }

    @Override
    public CaptainRequestResponseDto toDTO(CaptainRequest entity) {
        if (entity == null) return null;
        Campaign campaign      = entity.getIdCampaign();
        UserEntity coordinator = entity.getIdCoordinator();
        CaptainRequestResponseDto dto = new CaptainRequestResponseDto();
        dto.setId(entity.getId());
        dto.setName(nullSafe(entity.getName()));
        dto.setEmail(nullSafe(entity.getEmail()));
        dto.setIdCampaign(campaign != null ? campaign.getId() : null);
        dto.setCampaignName(nullSafe(campaign != null ? campaign.getName() : null));
        dto.setIdCoordinator(coordinator != null ? coordinator.getIdUser() : null);
        dto.setCoordinatorName(nullSafe(coordinator != null ? coordinator.getName() : null));
        dto.setStatus(nullSafe(entity.getStatus()));
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setResolvedAt(entity.getResolvedAt());
        return dto;
    }
}
