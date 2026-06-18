/**
 * DTO de respuesta para tiendas.
 *
 * Autores:
 * - Alejandra Ortiz: 100%
 */
package es.grupo8.backend.dto;

public record StoreResponseDto(
        Integer id,
        String name,
        String address,
        String postalCode,
        Integer chainId,
        String locality,
        Integer localityId,
        String zone,
        Integer zoneId,
        String chainName
) {
}
