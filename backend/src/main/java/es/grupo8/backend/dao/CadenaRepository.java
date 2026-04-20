package es.grupo8.backend.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.CadenaEntity;

public interface CadenaRepository extends JpaRepository<CadenaEntity, Integer> {

    Optional<CadenaEntity> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}