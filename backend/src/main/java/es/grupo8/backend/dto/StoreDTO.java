/**
 * DTO para transferir datos de tiendas.
 *
 * Autores:
 * - Alejandra Ortiz: 100%
 */
package es.grupo8.backend.dto;

import lombok.Data;

@Data
public class StoreDTO {

    // Siempre presente en GET responses
    private Integer id;

    // Input y output
    private String  name;
    private String  address;
    private String  postalCode;
    private Integer chainId;

    // GET-only (computado en StoreMapper)
    private String  locality;
    private Integer localityId;
    private String  zone;
    private Integer zoneId;
    private String  chainName;
}