package es.grupo8.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.Captain;
import es.grupo8.backend.entity.CaptainId;

public interface CaptainRepository extends JpaRepository<Captain, CaptainId> {

	@Query("SELECT COUNT(c) > 0 FROM Captain c WHERE c.idUser.idUser = :userId")
	boolean isUserCaptain(@Param("userId") Integer userId);

	@Query("SELECT c.idCampaign FROM Captain c WHERE c.idUser.idUser = :userId")
	List<Campaign> findCampaignsByUserId(@Param("userId") Integer userId);

	List<Captain> findByIdIdCampaign(Integer idCampaign);

	boolean existsByIdIdUserAndIdIdCampaign(Integer idUser, Integer idCampaign);

	@Transactional
	@Modifying
	void deleteByIdIdUserAndIdIdCampaign(Integer idUser, Integer idCampaign);

	@Transactional
	@Modifying
	void deleteAllByIdIdCampaign(Integer idCampaign);
}