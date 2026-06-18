/**
 * Autores:
 * - Alfonso Ramos Rojas: 90%
 * - IA Generativa: 10%
 */
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

    @Query(value = "SELECT pem.* FROM partner_entity_managers pem "
         + "LEFT JOIN user_accounts ua ON pem.id_user = ua.id_user "
         + "LEFT JOIN partner_entities pe ON pem.id_partner_entity = pe.id_partner_entity "
         + "WHERE (:search IS NULL OR :search = '' "
         + "   OR LOWER(ua.name) LIKE LOWER(CONCAT('%', :search, '%')) "
         + "   OR LOWER(ua.email) LIKE LOWER(CONCAT('%', :search, '%')) "
         + "   OR LOWER(pe.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
         + "ORDER BY pem.id_user ASC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<PartnerEntityManager> findAllByIdAsc(@Param("search") String search, @Param("size") int size, @Param("offset") int offset);

    @Query(value = "SELECT pem.* FROM partner_entity_managers pem "
         + "LEFT JOIN user_accounts ua ON pem.id_user = ua.id_user "
         + "LEFT JOIN partner_entities pe ON pem.id_partner_entity = pe.id_partner_entity "
         + "WHERE (:search IS NULL OR :search = '' "
         + "   OR LOWER(ua.name) LIKE LOWER(CONCAT('%', :search, '%')) "
         + "   OR LOWER(ua.email) LIKE LOWER(CONCAT('%', :search, '%')) "
         + "   OR LOWER(pe.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
         + "ORDER BY pem.id_user DESC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<PartnerEntityManager> findAllByIdDesc(@Param("search") String search, @Param("size") int size, @Param("offset") int offset);

    @Query(value = "SELECT pem.* FROM partner_entity_managers pem "
         + "LEFT JOIN user_accounts ua ON pem.id_user = ua.id_user "
         + "LEFT JOIN partner_entities pe ON pem.id_partner_entity = pe.id_partner_entity "
         + "WHERE (:search IS NULL OR :search = '' "
         + "   OR LOWER(ua.name) LIKE LOWER(CONCAT('%', :search, '%')) "
         + "   OR LOWER(ua.email) LIKE LOWER(CONCAT('%', :search, '%')) "
         + "   OR LOWER(pe.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
         + "ORDER BY ua.name ASC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<PartnerEntityManager> findAllByNameAsc(@Param("search") String search, @Param("size") int size, @Param("offset") int offset);

    @Query(value = "SELECT pem.* FROM partner_entity_managers pem "
         + "LEFT JOIN user_accounts ua ON pem.id_user = ua.id_user "
         + "LEFT JOIN partner_entities pe ON pem.id_partner_entity = pe.id_partner_entity "
         + "WHERE (:search IS NULL OR :search = '' "
         + "   OR LOWER(ua.name) LIKE LOWER(CONCAT('%', :search, '%')) "
         + "   OR LOWER(ua.email) LIKE LOWER(CONCAT('%', :search, '%')) "
         + "   OR LOWER(pe.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
         + "ORDER BY ua.name DESC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<PartnerEntityManager> findAllByNameDesc(@Param("search") String search, @Param("size") int size, @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) FROM partner_entity_managers pem "
         + "LEFT JOIN user_accounts ua ON pem.id_user = ua.id_user "
         + "LEFT JOIN partner_entities pe ON pem.id_partner_entity = pe.id_partner_entity "
         + "WHERE (:search IS NULL OR :search = '' "
         + "   OR LOWER(ua.name) LIKE LOWER(CONCAT('%', :search, '%')) "
         + "   OR LOWER(ua.email) LIKE LOWER(CONCAT('%', :search, '%')) "
         + "   OR LOWER(pe.name) LIKE LOWER(CONCAT('%', :search, '%')))", nativeQuery = true)
    long countWithSearch(@Param("search") String search);
}
