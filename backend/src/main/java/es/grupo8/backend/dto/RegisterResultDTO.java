/**
 * DTO con el resultado de un registro.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.dto;

/**
 * Result of submitting a captain registration request.
 *
 * @param message   human-readable result message
 * @param requestId identifier of the created captain request
 */
public record RegisterResultDTO(
        String message,
        Integer requestId
) {}
