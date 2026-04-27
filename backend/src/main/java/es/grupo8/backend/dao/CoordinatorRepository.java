package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.grupo8.backend.entity.Coordinator;
import es.grupo8.backend.entity.CoordinatorId;

@Repository
public interface CoordinatorRepository extends JpaRepository<Coordinator, CoordinatorId> {

    // Tu método (HEAD) - Lo mantenemos para que tu Controlador siga funcionando
    @Query("SELECT COUNT(c) > 0 FROM Coordinator c WHERE c.idUser.idUser = :userId")
    boolean isUserCoordinator(@Param("userId") Integer userId);

    // Los métodos de tu compañero (dev) - Los mantenemos para no romper su parte
    boolean existsByIdUser_IdUser(Integer userId);

    boolean existsByIdUser_IdUserAndIdCampaign_Id(Integer userId, Integer campaignId);

}