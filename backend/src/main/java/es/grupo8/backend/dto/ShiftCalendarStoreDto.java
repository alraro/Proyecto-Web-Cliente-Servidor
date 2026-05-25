package es.grupo8.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO representing a store entry in the calendar view, grouping its days and shifts.
 */
@Data
public class ShiftCalendarStoreDto {
    private Integer storeId;
    private String storeName;
    private List<ShiftCalendarDayDto> days;
}
