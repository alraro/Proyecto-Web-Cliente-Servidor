package es.grupo8.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.Coordinator;
import es.grupo8.backend.entity.CoordinatorId;

@Repository
public interface CoordinatorRepository extends JpaRepository<Coordinator, CoordinatorId> {

    // Tu método (HEAD) - Lo mantenemos para que tu Controlador siga funcionando
    @Query("SELECT COUNT(c) > 0 FROM Coordinator c WHERE c.idUser.idUser = :userId")
    boolean isUserCoordinator(@Param("userId") Integer userId);

    @Query("SELECT c.idCampaign FROM Coordinator c WHERE c.idUser.idUser = :userId")
    List<Campaign> findCampaignsByUserId(@Param("userId") Integer userId);

    // Los métodos de tu compañero (dev) - Los mantenemos para no romper su parte
    boolean existsByIdUser_IdUser(Integer userId);

    boolean existsByIdUser_IdUserAndIdCampaign_Id(Integer userId, Integer campaignId);

    // Find all coordinators for a specific campaign
    List<Coordinator> findByIdIdCampaign(Integer campaignId);

    // Check if a user is assigned as coordinator for a specific campaign
    boolean existsByIdIdUserAndIdIdCampaign(Integer userId, Integer campaignId);

    // Delete coordinator assignment for a user in a specific campaign
    @Transactional
    @Modifying
    void deleteByIdIdUserAndIdIdCampaign(Integer userId, Integer campaignId);

    // Delete all coordinator assignments for a specific campaign
    @Transactional
    @Modifying
    void deleteAllByIdIdCampaign(Integer idCampaign);

}