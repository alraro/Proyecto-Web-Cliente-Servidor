package es.grupo8.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import es.grupo8.backend.entity.Coordinator;
import es.grupo8.backend.entity.CoordinatorId;

public interface CoordinatorRepository extends JpaRepository<Coordinator, CoordinatorId> {

	List<Coordinator> findByIdIdCampaign(Integer idCampaign);

	boolean existsByIdIdUserAndIdIdCampaign(Integer idUser, Integer idCampaign);

	@Transactional
	@Modifying
	void deleteByIdIdUserAndIdIdCampaign(Integer idUser, Integer idCampaign);
}