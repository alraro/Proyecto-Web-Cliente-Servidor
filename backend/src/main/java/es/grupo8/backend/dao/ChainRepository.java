package es.grupo8.backend.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.ChainEntity;

public interface ChainRepository extends JpaRepository<ChainEntity, Integer> {

    Optional<ChainEntity> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}