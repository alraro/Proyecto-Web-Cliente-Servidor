/**
 * Repositorio JPA de acceso a datos de campañas.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 70%
 * - Alfonso Ramos Rojas: 20%
 * - IA Generativa: 10%
 */
package es.grupo8.backend.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.grupo8.backend.entity.Campaign;

public interface CampaignRepository extends JpaRepository<Campaign, Integer> {

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Integer id);

	List<Campaign> findByOrderByStartDateDesc();

	List<Campaign> findByIdTypeId(Integer id);

	boolean existsByIdTypeId(Integer id);

	Page<Campaign> findAll(Pageable pageable);

	Page<Campaign> findByEndDateBefore(LocalDate date, Pageable pageable);

	Page<Campaign> findByStartDateAfter(LocalDate date, Pageable pageable);

	Page<Campaign> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate d1, LocalDate d2, Pageable pageable);

	long countByEndDateBefore(LocalDate date);

	long countByStartDateAfter(LocalDate date);

	long countByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate d1, LocalDate d2);

	@Query(value = "SELECT * FROM campaigns ORDER BY id_campaign ASC LIMIT 1", nativeQuery = true)
	Campaign findFirstCampaign();
}