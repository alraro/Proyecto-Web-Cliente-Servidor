/**
 * DTO para exportar datos de campañas.
 *
 * Autores:
 * - Hugo Herrero González: 85%
 * - Fernando Luis Pinilla Molina: 5%
 * - IA Generativa: 10%
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