package es.grupo8.backend.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.grupo8.backend.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

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
}
