package es.grupo8.backend.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.grupo8.backend.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM administradores a WHERE a.id_usuario = :userId)", nativeQuery = true)
    boolean isAdministrador(@Param("userId") Integer userId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM coordinadores c WHERE c.id_usuario = :userId)", nativeQuery = true)
    boolean isCoordinador(@Param("userId") Integer userId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM capitanes c WHERE c.id_usuario = :userId)", nativeQuery = true)
    boolean isCapitan(@Param("userId") Integer userId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM responsable_entidad_colaboradora r WHERE r.id_usuario = :userId)", nativeQuery = true)
    boolean isResponsableEntidad(@Param("userId") Integer userId);
}
