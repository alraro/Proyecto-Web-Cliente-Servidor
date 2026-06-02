package es.grupo8.backend.mapper;
import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.AdminDTO;

@Component
public class AdminMapper{
    
    public AdminDTO toCoverageDTO(String label, long covered, long total) {
        double percentage = total > 0 ? Math.round(((double) covered / total) * 100.0) : 0.0;

        AdminDTO dto = new AdminDTO();
        dto.setLabel(label);
        dto.setStoresInCampaign(covered);
        dto.setTotalStores(total);
        dto.setCoveragePercentage(percentage);

        return dto;
    }

}
