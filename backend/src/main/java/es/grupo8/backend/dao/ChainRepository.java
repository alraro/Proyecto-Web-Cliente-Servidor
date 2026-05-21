package es.grupo8.backend.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.ChainEntity;

public interface ChainRepository extends JpaRepository<ChainEntity, Integer> {

    List<ChainEntity> findAllByOrderByIdChainAsc();

    Optional<ChainEntity> findByCode(String code);

    boolean existsByCode(String code);
}