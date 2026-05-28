package es.grupo8.backend.dto;

import lombok.Data;


//DTO for submitting a captain application request.
 
@Data
public class CaptainRequestDto {
    private String name;
    private String email;
    private String password;
    private Integer idCampaign;
}
