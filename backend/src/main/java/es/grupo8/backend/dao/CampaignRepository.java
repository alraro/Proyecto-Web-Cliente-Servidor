package es.grupo8.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.Campaign;

public interface CampaignRepository extends JpaRepository<Campaign, Integer> {

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Integer id);

	List<Campaign> findByOrderByStartDateDesc();

	List<Campaign> findByIdTypeId(Integer id);

	boolean existsByIdTypeId(Integer id);
}