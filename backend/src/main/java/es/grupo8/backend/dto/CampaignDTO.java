package es.grupo8.backend.dto;

import java.time.LocalDate;
import java.util.Set;

import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.entity.CampaignType;
import lombok.Data;

@Data
public class CampaignDTO {
    private Integer id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private CampaignType idType;
    private Set<Store> stores;
    private Set<UserEntity> captains;
    private Set<UserEntity> coordinators;
    private long storesInCampaign;
}