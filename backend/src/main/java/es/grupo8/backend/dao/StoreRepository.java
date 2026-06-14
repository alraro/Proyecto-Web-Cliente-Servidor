/*
* Autores:
* - Alejandra Ortiz Robles: 60%
* - Hugo Herrero González: 30%
* - IA Generativa: 10%
* 
*/
package es.grupo8.backend.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.grupo8.backend.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Integer> {

    List<Store> findAllByOrderByIdAsc();

    List<Store> findByIdChain_IdChain(Integer chainId);

    @Query("SELECT s FROM Store s WHERE s.postalCode.idLocality.id = :localityId")
    List<Store> findByLocalityId(@Param("localityId") Integer localityId);

    @Query("SELECT s FROM Store s WHERE s.postalCode.idLocality.idZone.id = :zoneId")
    List<Store> findByZoneId(@Param("zoneId") Integer zoneId);

    @Query("SELECT s FROM Store s WHERE s.idChain.idChain = :chainId AND s.postalCode.idLocality.id = :localityId")
    List<Store> findByChainAndLocality(@Param("chainId") Integer chainId,
                                       @Param("localityId") Integer localityId);

    @Query("SELECT s FROM Store s WHERE s.idChain.idChain = :chainId AND s.postalCode.idLocality.idZone.id = :zoneId")
    List<Store> findByChainAndZone(@Param("chainId") Integer chainId,
                                   @Param("zoneId") Integer zoneId);

    /* Check if a user is the assigned manager of a specific store */
    boolean existsByIdAndIdResponsible_IdUser(Integer storeId, Integer userId);

    /* Find the store assigned to a responsible user (used at login) */
    java.util.Optional<Store> findByIdResponsible_IdUser(Integer userId);

    /* Check if a user is responsible for any store */
    boolean existsByIdResponsible_IdUser(Integer userId);


   @Query("SELECT s.idChain.name AS chainName, " +
           "SUM(CASE WHEN s IN (SELECT st FROM Campaign c JOIN c.stores st WHERE c.id = :campaignId) THEN 1 ELSE 0 END) AS covered, " +
           "COUNT(s) AS total " +
           "FROM Store s GROUP BY s.idChain.name")
    List<Map<String, Object>> findChainCoverageCampaign(@Param("campaignId") Integer campaignId);

    @Query("SELECT s.postalCode.idLocality.name AS localityName, " +
           "SUM(CASE WHEN s IN (SELECT st FROM Campaign c JOIN c.stores st WHERE c.id = :campaignId) THEN 1 ELSE 0 END) AS covered, " +
           "COUNT(s) AS total " +
           "FROM Store s GROUP BY s.postalCode.idLocality.name")
    List<Map<String, Object>> findLocalityCoverageCampaign(@Param("campaignId") Integer campaignId);

    @Query("SELECT s.postalCode.idLocality.idZone.name AS zoneName, " +
           "SUM(CASE WHEN s IN (SELECT st FROM Campaign c JOIN c.stores st WHERE c.id = :campaignId) THEN 1 ELSE 0 END) AS covered, " +
           "COUNT(s) AS total " +
           "FROM Store s GROUP BY s.postalCode.idLocality.idZone.name")
    List<Map<String, Object>> findZoneCoverageCampaign(@Param("campaignId") Integer campaignId);

    @Query(value = "SELECT * FROM stores WHERE id_responsible IS NULL ORDER BY id_store ASC LIMIT 1", nativeQuery = true)
    Store findFirstWithoutResponsible();
}