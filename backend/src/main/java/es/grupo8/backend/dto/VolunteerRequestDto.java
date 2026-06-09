package es.grupo8.backend.dto;

import lombok.Data;

/**
 * DTO for creating or updating a volunteer.
 * Used by coordinator endpoints POST /api/coordinator/volunteers and PUT /api/coordinator/volunteers/{id}.
 */
@Data
public class VolunteerRequestDto {
    private String name;
    private String phone;
    private String email;
    private String address;
    private Integer partnerEntityId;
}
