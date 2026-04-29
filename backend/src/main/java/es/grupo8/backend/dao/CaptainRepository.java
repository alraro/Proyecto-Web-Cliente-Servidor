package es.grupo8.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import es.grupo8.backend.entity.Captain;
import es.grupo8.backend.entity.CaptainId;

public interface CaptainRepository extends JpaRepository<Captain, CaptainId> {

	List<Captain> findByIdIdCampaign(Integer idCampaign);

	boolean existsByIdIdUserAndIdIdCampaign(Integer idUser, Integer idCampaign);

	@Transactional
	@Modifying
	void deleteByIdIdUserAndIdIdCampaign(Integer idUser, Integer idCampaign);

	@Transactional
	@Modifying
	void deleteAllByIdIdCampaign(Integer idCampaign);
}