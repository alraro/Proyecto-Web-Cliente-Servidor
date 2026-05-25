package es.grupo8.backend.dto;

import lombok.Data;

/**
 * Minimal DTO for returning store id and name, used in campaign store listings.
 */
@Data
public class StoreSimpleDto {
    private Integer id;
    private String name;
}
