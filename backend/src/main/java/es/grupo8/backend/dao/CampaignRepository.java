package es.grupo8.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.Campaign;

public interface CampaignRepository extends JpaRepository<Campaign, Integer> {

	boolean existsByName(String name);

	boolean existsByNameAndIdCampaignNot(String name, Integer idCampaign);

	List<Campaign> findByOrderByStartDateDesc();

	List<Campaign> findByTypeIdType(Integer idType);

	boolean existsByTypeIdType(Integer idType);
}