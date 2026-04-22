package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.AdminEntity;

public interface AdminRepository extends JpaRepository<AdminEntity, Integer> {

    // Spring Data generates automatically: SELECT COUNT(*) > 0 FROM administrators
    // WHERE id_user = ? - no native query, no SQL injection risk.
    boolean existsByIdUser(Integer idUser);
}