package es.grupo8.backend.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import es.grupo8.backend.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Integer>, JpaSpecificationExecutor<UserEntity> {

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

    @Query(value = "SELECT u.* FROM user_accounts u INNER JOIN coordinators c ON u.id_user = c.id_user GROUP BY u.id_user", nativeQuery = true)
    List<UserEntity> findAllCoordinators();

    @Query(value = "SELECT u.* FROM user_accounts u INNER JOIN captains c ON u.id_user = c.id_user GROUP BY u.id_user", nativeQuery = true)
    List<UserEntity> findAllCaptains();
}
