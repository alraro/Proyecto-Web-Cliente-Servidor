package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminRepository extends JpaRepository<Object, Integer> {

    // Comprueba si un ID de usuario existe en la tabla Administradores.
    // Query nativa porque la tabla no tiene entidad JPA propia — es una
    // tabla de una sola columna que actúa como lista de roles.
    @Query(value = "SELECT COUNT(*) > 0 FROM administradores WHERE id_usuario = :userId",
           nativeQuery = true)
    boolean existsAdminById(@Param("userId") Integer userId);
}