package es.grupo8.backend.dto;

import lombok.Data;

@Data
public class AvailableVolunteerDto {
    private Integer volunteerId;
    private String name;
    private String email;
    private String phone;
    private String partnerEntityName;
}
