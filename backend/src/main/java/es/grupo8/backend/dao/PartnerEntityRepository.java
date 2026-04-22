package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.PartnerEntity;

public interface PartnerEntityRepository extends JpaRepository<PartnerEntity, Integer> {
}