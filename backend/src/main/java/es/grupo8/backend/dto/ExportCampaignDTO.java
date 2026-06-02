/*
*
* Autores:
*	- Hugo Herrero González: 100%
*/
package es.grupo8.backend.dto;
import lombok.Data;

@Data
public class ExportCampaignDTO {
    private Integer id;
    private String name;
    private String type;
    private String startDate;
    private String endDate;
}