package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.PartnerEntityManager;

public interface PartnerEntityManagerRepository extends JpaRepository<PartnerEntityManager, Integer> {
}