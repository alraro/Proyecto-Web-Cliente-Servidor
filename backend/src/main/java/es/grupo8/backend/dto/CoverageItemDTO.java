/*
*
* Autores:
*	- Hugo Herrero González: 100%
*/
package es.grupo8.backend.dto;
import lombok.Data;

@Data
public class CoverageItemDTO {
    private String label;
    private long totalStores;
    private long storesInCampaign;
}
