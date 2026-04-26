package es.grupo8.backend.dao;
 
import java.util.List;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.grupo8.backend.entity.CampaignStore;
import es.grupo8.backend.entity.CampaignStoreId;
 
public interface CampaignStoreRepository extends JpaRepository<CampaignStore, CampaignStoreId> {
 
    /** Devuelve todas las CampaignStore asociadas a una tienda concreta */
    List<CampaignStore> findByIdStore_Id(Integer storeId);

    // Cobertura por Cadena para una campaña concreta
    @Query("select s.idChain.name, COUNT(DISTINCT s.id), COUNT(DISTINCT cs.idStore.id) " +
            "from Store s " +
            "left join CampaignStore cs ON cs.idStore.id = s.id AND cs.idCampaign.id = :campaignId " +
            "where s.idChain IS NOT NULL " +
            "group by s.idChain.id, s.idChain.name " +
            "order by s.idChain.name")
    List<Object[]> coverageByChain(@Param("campaignId") Integer campaignId);


    // Cobertura por Localidad para una campaña concreta
    @Query("select s.postalCode.idLocality.name, count(distinct s.id), count(distinct cs.idStore.id) " +
            "from Store s " +
            "left join CampaignStore cs ON cs.idStore.id = s.id AND cs.idCampaign.id = :campaignId " +
            "where s.postalCode is not null " +
            "group by s.postalCode.idLocality.id, s.postalCode.idLocality.name " +
            "order by s.postalCode.idLocality.name")
    List<Object[]> coverageByLocality(@Param("campaignId") Integer campaignId);

    // Cobertura por zona geográfica para una campaña concreta
    @Query("select s.postalCode.idLocality.idZone.name, count(distinct s.id), count(distinct cs.idStore.id) " +
            "from Store s " +
            "left join CampaignStore cs ON cs.idStore.id = s.id AND cs.idCampaign.id = :campaignId " +
            "where s.postalCode is not null and s.postalCode.idLocality.idZone is not null " +
            "group by s.postalCode.idLocality.idZone.id, s.postalCode.idLocality.idZone.name " +
            "order by s.postalCode.idLocality.idZone.name")
    List<Object[]> coverageByZone(@Param("campaignId") Integer campaignId);

    // Cobertura total para una campaña concreta
    @Query("select c.id, c.name, c.startDate, c.endDate, count(distinct cs.idStore.id)" +
            "from Campaign c " +
            "left join CampaignStore cs ON cs.idCampaign.id = c.id " +
            "group by c.id, c.name, c.startDate, c.endDate " +
            "order by c.startDate desc")
    List<Object[]> coverageAllCampaigns();
}