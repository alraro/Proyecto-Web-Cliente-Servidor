package es.grupo8.backend.dto;

import lombok.Data;
import java.time.Instant;

// DTO for returning captain application request data. Password hash is omitted.
@Data
public class CaptainRequestResponseDto {
    private Integer id;
    private String name;
    private String email;
    private Integer idCampaign;
    private String campaignName;
    private Integer idCoordinator;
    private String coordinatorName;
    private String status;
    private Instant createdAt;
    private Instant resolvedAt;
}
