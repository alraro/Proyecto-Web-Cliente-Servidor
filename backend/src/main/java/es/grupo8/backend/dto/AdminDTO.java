/*
* Autores:
* - Hugo Herrero González: 100%
 */

package es.grupo8.backend.dto;

import lombok.Data;

@Data
public class AdminDTO {
    private String label;
    private long storesInCampaign;
    private long totalStores;
    private double coveragePercentage;
}
