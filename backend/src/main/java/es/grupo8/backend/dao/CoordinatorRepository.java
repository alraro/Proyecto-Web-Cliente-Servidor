package es.grupo8.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import es.grupo8.backend.entity.Coordinator;
import es.grupo8.backend.entity.CoordinatorId;

public interface CoordinatorRepository extends JpaRepository<Coordinator, CoordinatorId> {

    // Validar si el usuario es coordinador de alguna campaña
    boolean existsByIdUser_IdUser(Integer userId);

    // Comprobar si es coordinador de una campaña concreta
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