package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.grupo8.backend.entity.Coordinator;
import es.grupo8.backend.entity.CoordinatorId;

@Repository
public interface CoordinatorRepository extends JpaRepository<Coordinator, CoordinatorId> {

    // Check if user is coordinator of any campaign
    @Query("SELECT COUNT(c) > 0 FROM Coordinator c WHERE c.idUser.idUser = :userId")
    boolean isUserCoordinator(@Param("userId") Integer userId);
}