package es.grupo8.backend.mapper;

import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.ShiftResponseDto;
import es.grupo8.backend.entity.Shift;

@Component
public class ShiftMapper extends MapperDTO<ShiftResponseDto, Shift> {

    /**
     * Maps a single Shift entity to its response DTO.
     *
     * @param shift the Shift entity to map
     * @return the populated ShiftResponseDto, or null if the entity is null
     */
    @Override
    public ShiftResponseDto toDTO(Shift shift) {
        if (shift == null) return null;

        ShiftResponseDto dto = new ShiftResponseDto();

        dto.setShiftId(shift.getId());
        dto.setCampaignId(shift.getIdCampaign().getId());
        dto.setCampaignName(shift.getIdCampaign().getName());
        dto.setStoreId(shift.getIdStore().getId());
        dto.setStoreName(shift.getIdStore().getName());
        dto.setDay(shift.getShiftDay());
        dto.setStartTime(shift.getStartTime());
        dto.setEndTime(shift.getEndTime());
        dto.setVolunteersNeeded(shift.getVolunteersNeeded());
        dto.setLocation(shift.getLocation() != null ? shift.getLocation() : "");
        dto.setObservations(shift.getObservations() != null ? shift.getObservations() : "");

        return dto;
    }
}
