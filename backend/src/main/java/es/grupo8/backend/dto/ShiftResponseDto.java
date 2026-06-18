/**
 * Autores:
 * - Alejandro Calvo Aguilar: 100%
 */
package es.grupo8.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;


@Data
public class ShiftResponseDto {
    private Integer shiftId;
    private Integer campaignId;
    private String campaignName;
    private Integer storeId;
    private String storeName;
    private LocalDate day;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer volunteersNeeded;
    private String location;
    private String observations;
}
