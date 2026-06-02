/*
*
* Autores:
*	- Hugo Herrero González: 50%
*   - Fernando Luis Pinilla Molina: 50%
*/
package es.grupo8.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CampaignDTO {
    private Integer id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private long storesInCampaign;
    private String status;
    private Integer typeId;
    private String typeName;
}
