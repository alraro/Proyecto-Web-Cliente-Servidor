/**
 * DTO para transferir datos de incidencias.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.dto;

import lombok.Data;

@Data
public class IncidentDTO {
    private Integer id;
    private String description;
    private String createdAt;
    private String campaignName;
    private String storeName;
    private String captainName;
}
