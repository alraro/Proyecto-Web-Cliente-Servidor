package es.grupo8.backend.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.grupo8.backend.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    List<UserEntity> findAllByOrderByIdUserAsc();

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM administrators a WHERE a.id_user = :userId)", nativeQuery = true)
    boolean isAdmin(@Param("userId") Integer userId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM coordinators c WHERE c.id_user = :userId)", nativeQuery = true)
    boolean isCoordinator(@Param("userId") Integer userId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM captains c WHERE c.id_user = :userId)", nativeQuery = true)
    boolean isCaptain(@Param("userId") Integer userId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM partner_entity_managers r WHERE r.id_user = :userId)", nativeQuery = true)
    boolean isPartnerEntityManager(@Param("userId") Integer userId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM partner_entity_managers r WHERE r.id_user = :userId AND r.id_partner_entity = :entityId)", nativeQuery = true)
    boolean isPartnerEntityManagerOfEntity(@Param("userId") Integer userId, @Param("entityId") Integer entityId);

    @Query(value = "SELECT u.* FROM user_accounts u INNER JOIN coordinators c ON u.id_user = c.id_user GROUP BY u.id_user", nativeQuery = true)
    List<UserEntity> findAllCoordinators();

    @Query(value = "SELECT u.* FROM user_accounts u INNER JOIN captains c ON u.id_user = c.id_user GROUP BY u.id_user", nativeQuery = true)
    List<UserEntity> findAllCaptains();

    @Query("SELECT u FROM UserEntity u WHERE u.idUser IN (SELECT c.id.idUser FROM Coordinator c) "
         + "AND u.idUser NOT IN (SELECT c2.id.idUser FROM Coordinator c2 WHERE c2.id.idCampaign = :campaignId)")
    List<UserEntity> findAvailableCoordinators(@Param("campaignId") Integer campaignId);

    @Query("SELECT u FROM UserEntity u WHERE u.idUser IN (SELECT c.id.idUser FROM Captain c) "
         + "AND u.idUser NOT IN (SELECT c2.id.idUser FROM Captain c2 WHERE c2.id.idCampaign = :campaignId)")
    List<UserEntity> findAvailableCaptains(@Param("campaignId") Integer campaignId);

    @Query(value = """
            SELECT ua.* FROM user_accounts ua
            WHERE (:search IS NULL OR :search = ''
               OR LOWER(ua.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(ua.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:role IS NULL OR :role = '' OR :role = 'ALL'
               OR (:role = 'ADMINISTRADOR' AND EXISTS (SELECT 1 FROM administrators a WHERE a.id_user = ua.id_user))
               OR (:role = 'COORDINADOR' AND EXISTS (SELECT 1 FROM coordinators c WHERE c.id_user = ua.id_user))
               OR (:role = 'CAPITAN' AND EXISTS (SELECT 1 FROM captains c WHERE c.id_user = ua.id_user))
               OR (:role = 'COLABORADOR' AND EXISTS (SELECT 1 FROM partner_entity_managers pem WHERE pem.id_user = ua.id_user))
               OR (:role = 'RESPONSABLE_TIENDA' AND EXISTS (SELECT 1 FROM stores s WHERE s.id_responsible = ua.id_user)))
            ORDER BY ua.id_user ASC LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<UserEntity> findAllByIdAsc(@Param("search") String search, @Param("role") String role,
                                    @Param("size") int size, @Param("offset") int offset);

    @Query(value = """
            SELECT ua.* FROM user_accounts ua
            WHERE (:search IS NULL OR :search = ''
               OR LOWER(ua.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(ua.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:role IS NULL OR :role = '' OR :role = 'ALL'
               OR (:role = 'ADMINISTRADOR' AND EXISTS (SELECT 1 FROM administrators a WHERE a.id_user = ua.id_user))
               OR (:role = 'COORDINADOR' AND EXISTS (SELECT 1 FROM coordinators c WHERE c.id_user = ua.id_user))
               OR (:role = 'CAPITAN' AND EXISTS (SELECT 1 FROM captains c WHERE c.id_user = ua.id_user))
               OR (:role = 'COLABORADOR' AND EXISTS (SELECT 1 FROM partner_entity_managers pem WHERE pem.id_user = ua.id_user))
               OR (:role = 'RESPONSABLE_TIENDA' AND EXISTS (SELECT 1 FROM stores s WHERE s.id_responsible = ua.id_user)))
            ORDER BY ua.id_user DESC LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<UserEntity> findAllByIdDesc(@Param("search") String search, @Param("role") String role,
                                     @Param("size") int size, @Param("offset") int offset);

    @Query(value = """
            SELECT ua.* FROM user_accounts ua
            WHERE (:search IS NULL OR :search = ''
               OR LOWER(ua.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(ua.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:role IS NULL OR :role = '' OR :role = 'ALL'
               OR (:role = 'ADMINISTRADOR' AND EXISTS (SELECT 1 FROM administrators a WHERE a.id_user = ua.id_user))
               OR (:role = 'COORDINADOR' AND EXISTS (SELECT 1 FROM coordinators c WHERE c.id_user = ua.id_user))
               OR (:role = 'CAPITAN' AND EXISTS (SELECT 1 FROM captains c WHERE c.id_user = ua.id_user))
               OR (:role = 'COLABORADOR' AND EXISTS (SELECT 1 FROM partner_entity_managers pem WHERE pem.id_user = ua.id_user))
               OR (:role = 'RESPONSABLE_TIENDA' AND EXISTS (SELECT 1 FROM stores s WHERE s.id_responsible = ua.id_user)))
            ORDER BY ua.name ASC LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<UserEntity> findAllByNameAsc(@Param("search") String search, @Param("role") String role,
                                      @Param("size") int size, @Param("offset") int offset);

    @Query(value = """
            SELECT ua.* FROM user_accounts ua
            WHERE (:search IS NULL OR :search = ''
               OR LOWER(ua.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(ua.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:role IS NULL OR :role = '' OR :role = 'ALL'
               OR (:role = 'ADMINISTRADOR' AND EXISTS (SELECT 1 FROM administrators a WHERE a.id_user = ua.id_user))
               OR (:role = 'COORDINADOR' AND EXISTS (SELECT 1 FROM coordinators c WHERE c.id_user = ua.id_user))
               OR (:role = 'CAPITAN' AND EXISTS (SELECT 1 FROM captains c WHERE c.id_user = ua.id_user))
               OR (:role = 'COLABORADOR' AND EXISTS (SELECT 1 FROM partner_entity_managers pem WHERE pem.id_user = ua.id_user))
               OR (:role = 'RESPONSABLE_TIENDA' AND EXISTS (SELECT 1 FROM stores s WHERE s.id_responsible = ua.id_user)))
            ORDER BY ua.name DESC LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<UserEntity> findAllByNameDesc(@Param("search") String search, @Param("role") String role,
                                       @Param("size") int size, @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*) FROM user_accounts ua
            WHERE (:search IS NULL OR :search = ''
               OR LOWER(ua.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(ua.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:role IS NULL OR :role = '' OR :role = 'ALL'
               OR (:role = 'ADMINISTRADOR' AND EXISTS (SELECT 1 FROM administrators a WHERE a.id_user = ua.id_user))
               OR (:role = 'COORDINADOR' AND EXISTS (SELECT 1 FROM coordinators c WHERE c.id_user = ua.id_user))
               OR (:role = 'CAPITAN' AND EXISTS (SELECT 1 FROM captains c WHERE c.id_user = ua.id_user))
               OR (:role = 'COLABORADOR' AND EXISTS (SELECT 1 FROM partner_entity_managers pem WHERE pem.id_user = ua.id_user))
               OR (:role = 'RESPONSABLE_TIENDA' AND EXISTS (SELECT 1 FROM stores s WHERE s.id_responsible = ua.id_user)))
            """, nativeQuery = true)
    long countUsers(@Param("search") String search, @Param("role") String role);
}
