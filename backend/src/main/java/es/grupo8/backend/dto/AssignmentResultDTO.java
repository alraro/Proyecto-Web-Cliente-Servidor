/**
 * DTO con el resultado de una asignación a campaña.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 90%
 * - IA Generativa: 10%
 */
package es.grupo8.backend.dto;

/**
 * Result of assigning a user (coordinator or captain) to a campaign.
 *
 * @param message    human-readable result message
 * @param campaignId target campaign identifier
 * @param userId     assigned user identifier
 * @param userName   assigned user name
 */
public record AssignmentResultDTO(
        String message,
        Integer campaignId,
        Integer userId,
        String userName
) {}
