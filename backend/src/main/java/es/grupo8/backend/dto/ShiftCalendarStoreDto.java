package es.grupo8.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ShiftCalendarStoreDto {
    private Integer storeId;
    private String storeName;
    private List<ShiftCalendarDayDto> days;
}
