package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.AdminEntity;

public interface AdminRepository extends JpaRepository<AdminEntity, Integer> {

    // Spring Data genera automáticamente: SELECT COUNT(*) > 0 FROM administradores
    // WHERE id_usuario = ? — sin query nativa, sin riesgo de SQL Injection.
    boolean existsByIdUsuario(Integer idUsuario);
}