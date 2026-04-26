package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.Coordinator;
import es.grupo8.backend.entity.CoordinatorId;

public interface CoordinatorRepository extends JpaRepository<Coordinator, CoordinatorId> {

    // Validar si el usuario es coordinador de alguna campaña
    boolean existsByIdUser_IdUser(Integer userId);

    // Comprobar si es coordinador de una campaña concreta
    boolean existsByIdUser_IdUserAndIdCampaign_Id(Integer userId, Integer campaignId);


}