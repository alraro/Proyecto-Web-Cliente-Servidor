package es.grupo8.backend.dao;

import es.grupo8.backend.entity.PartnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PartnerEntityRepository extends JpaRepository<PartnerEntity, Integer> {

    @Query(value = "SELECT * FROM partner_entities pe "
         + "WHERE (:search IS NULL OR :search = '' "
         + "   OR LOWER(pe.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
         + "ORDER BY pe.id_partner_entity ASC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<PartnerEntity> findAllByIdAsc(@Param("search") String search, @Param("size") int size, @Param("offset") int offset);

    @Query(value = "SELECT * FROM partner_entities pe "
         + "WHERE (:search IS NULL OR :search = '' "
         + "   OR LOWER(pe.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
         + "ORDER BY pe.id_partner_entity DESC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<PartnerEntity> findAllByIdDesc(@Param("search") String search, @Param("size") int size, @Param("offset") int offset);

    @Query(value = "SELECT * FROM partner_entities pe "
         + "WHERE (:search IS NULL OR :search = '' "
         + "   OR LOWER(pe.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
         + "ORDER BY pe.name ASC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<PartnerEntity> findAllByNameAsc(@Param("search") String search, @Param("size") int size, @Param("offset") int offset);

    @Query(value = "SELECT * FROM partner_entities pe "
         + "WHERE (:search IS NULL OR :search = '' "
         + "   OR LOWER(pe.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
         + "ORDER BY pe.name DESC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<PartnerEntity> findAllByNameDesc(@Param("search") String search, @Param("size") int size, @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) FROM partner_entities pe "
         + "WHERE (:search IS NULL OR :search = '' "
         + "   OR LOWER(pe.name) LIKE LOWER(CONCAT('%', :search, '%')))", nativeQuery = true)
    long countWithSearch(@Param("search") String search);
}
