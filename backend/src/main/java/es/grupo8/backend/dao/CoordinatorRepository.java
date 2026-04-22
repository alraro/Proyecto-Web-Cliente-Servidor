package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.Coordinator;
import es.grupo8.backend.entity.CoordinatorId;

public interface CoordinatorRepository extends JpaRepository<Coordinator, CoordinatorId> {
}