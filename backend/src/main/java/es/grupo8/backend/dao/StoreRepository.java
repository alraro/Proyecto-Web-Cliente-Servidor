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

    @Query(value = "SELECT s.* FROM stores s "
         + "LEFT JOIN chains c ON s.id_chain = c.id_chain "
         + "LEFT JOIN postal_codes pc ON s.postal_code = pc.postal_code "
         + "LEFT JOIN localities l ON pc.id_locality = l.id_locality "
         + "WHERE (:search IS NULL OR :search = '' "
         + "   OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
         + "AND (:chainId IS NULL OR s.id_chain = :chainId) "
         + "AND (:localityId IS NULL OR l.id_locality = :localityId) "
         + "AND (:zoneId IS NULL OR l.id_zone = :zoneId) "
         + "ORDER BY s.id_store ASC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<Store> findAllByIdAsc(@Param("search") String search,
                               @Param("chainId") Integer chainId,
                               @Param("localityId") Integer localityId,
                               @Param("zoneId") Integer zoneId,
                               @Param("size") int size,
                               @Param("offset") int offset);

    @Query(value = "SELECT s.* FROM stores s "
         + "LEFT JOIN chains c ON s.id_chain = c.id_chain "
         + "LEFT JOIN postal_codes pc ON s.postal_code = pc.postal_code "
         + "LEFT JOIN localities l ON pc.id_locality = l.id_locality "
         + "WHERE (:search IS NULL OR :search = '' "
         + "   OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
         + "AND (:chainId IS NULL OR s.id_chain = :chainId) "
         + "AND (:localityId IS NULL OR l.id_locality = :localityId) "
         + "AND (:zoneId IS NULL OR l.id_zone = :zoneId) "
         + "ORDER BY s.id_store DESC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<Store> findAllByIdDesc(@Param("search") String search,
                                @Param("chainId") Integer chainId,
                                @Param("localityId") Integer localityId,
                                @Param("zoneId") Integer zoneId,
                                @Param("size") int size,
                                @Param("offset") int offset);

    @Query(value = "SELECT s.* FROM stores s "
         + "LEFT JOIN chains c ON s.id_chain = c.id_chain "
         + "LEFT JOIN postal_codes pc ON s.postal_code = pc.postal_code "
         + "LEFT JOIN localities l ON pc.id_locality = l.id_locality "
         + "WHERE (:search IS NULL OR :search = '' "
         + "   OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
         + "AND (:chainId IS NULL OR s.id_chain = :chainId) "
         + "AND (:localityId IS NULL OR l.id_locality = :localityId) "
         + "AND (:zoneId IS NULL OR l.id_zone = :zoneId) "
         + "ORDER BY s.name ASC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<Store> findAllByNameAsc(@Param("search") String search,
                                 @Param("chainId") Integer chainId,
                                 @Param("localityId") Integer localityId,
                                 @Param("zoneId") Integer zoneId,
                                 @Param("size") int size,
                                 @Param("offset") int offset);

    @Query(value = "SELECT s.* FROM stores s "
         + "LEFT JOIN chains c ON s.id_chain = c.id_chain "
         + "LEFT JOIN postal_codes pc ON s.postal_code = pc.postal_code "
         + "LEFT JOIN localities l ON pc.id_locality = l.id_locality "
         + "WHERE (:search IS NULL OR :search = '' "
         + "   OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
         + "AND (:chainId IS NULL OR s.id_chain = :chainId) "
         + "AND (:localityId IS NULL OR l.id_locality = :localityId) "
         + "AND (:zoneId IS NULL OR l.id_zone = :zoneId) "
         + "ORDER BY s.name DESC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<Store> findAllByNameDesc(@Param("search") String search,
                                  @Param("chainId") Integer chainId,
                                  @Param("localityId") Integer localityId,
                                  @Param("zoneId") Integer zoneId,
                                  @Param("size") int size,
                                  @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) FROM stores s "
         + "LEFT JOIN chains c ON s.id_chain = c.id_chain "
         + "LEFT JOIN postal_codes pc ON s.postal_code = pc.postal_code "
         + "LEFT JOIN localities l ON pc.id_locality = l.id_locality "
         + "WHERE (:search IS NULL OR :search = '' "
         + "   OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
         + "AND (:chainId IS NULL OR s.id_chain = :chainId) "
         + "AND (:localityId IS NULL OR l.id_locality = :localityId) "
         + "AND (:zoneId IS NULL OR l.id_zone = :zoneId)", nativeQuery = true)
    long countWithFilters(@Param("search") String search,
                          @Param("chainId") Integer chainId,
                          @Param("localityId") Integer localityId,
                          @Param("zoneId") Integer zoneId);

    /* Comprueba si un usuario es el gerente asignado de una tienda específica */
    boolean existsByIdAndIdResponsible_IdUser(Integer storeId, Integer userId);

    /* Busca la tienda asignada a un usuario responsable (utilizada al iniciar sesión) */
    java.util.Optional<Store> findByIdResponsible_IdUser(Integer userId);

    /* Comprobar si un usuario es responsable de alguna tienda */
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