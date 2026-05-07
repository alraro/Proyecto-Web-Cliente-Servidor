package es.grupo8.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.grupo8.backend.entity.Incident;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Integer> {

    @Query("SELECT i FROM Incident i WHERE i.idCampaign.id = :campaignId AND i.idStore.id = :storeId ORDER BY i.createdAt DESC")
    List<Incident> findByCampaignAndStore(@Param("campaignId") Integer campaignId, @Param("storeId") Integer storeId);

    @Query("SELECT i FROM Incident i WHERE i.idUser.idUser = :userId ORDER BY i.createdAt DESC")
    List<Incident> findByUserId(@Param("userId") Integer userId);
}
