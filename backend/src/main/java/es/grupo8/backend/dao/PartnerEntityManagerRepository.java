package es.grupo8.backend.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.grupo8.backend.entity.PartnerEntityManager;

public interface PartnerEntityManagerRepository extends JpaRepository<PartnerEntityManager, Integer> {

    @Query("SELECT pem FROM PartnerEntityManager pem " +
	    "LEFT JOIN FETCH pem.userAccounts " +
	    "LEFT JOIN FETCH pem.idPartnerEntity")
    List<PartnerEntityManager> findAllWithRelations();

    @Query("SELECT pem FROM PartnerEntityManager pem " +
	    "LEFT JOIN FETCH pem.userAccounts " +
	    "LEFT JOIN FETCH pem.idPartnerEntity " +
	    "WHERE pem.id = :userId")
    Optional<PartnerEntityManager> findByIdWithRelations(@Param("userId") Integer userId);
}