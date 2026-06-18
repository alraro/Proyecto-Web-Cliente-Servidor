/**
 * Repositorio JPA de la asignación de capitanes a campañas.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 65%
 * - Alfonso Ramos Rojas: 25%
 * - IA Generativa: 10%
 */
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
import es.grupo8.backend.entity.UserEntity;

public interface CaptainRepository extends JpaRepository<Captain, CaptainId> {

	@Query("SELECT c.idCampaign FROM Captain c WHERE c.idUser.idUser = :userId")
	List<Campaign> findCampaignsByUserId(@Param("userId") Integer userId);

	// Users assigned as captains for a campaign
	@Query("SELECT c.idUser FROM Captain c WHERE c.id.idCampaign = :campaignId")
	List<UserEntity> findUsersByCampaignId(@Param("campaignId") Integer campaignId);

	List<Captain> findByIdIdCampaign(Integer idCampaign);

	boolean existsByIdIdUserAndIdIdCampaign(Integer idUser, Integer idCampaign);

	@Transactional
	@Modifying
	void deleteByIdIdUserAndIdIdCampaign(Integer idUser, Integer idCampaign);

	@Transactional
	@Modifying
	void deleteAllByIdIdCampaign(Integer idCampaign);
}