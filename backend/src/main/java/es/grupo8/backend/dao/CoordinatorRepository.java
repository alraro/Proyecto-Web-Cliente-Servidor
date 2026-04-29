package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.grupo8.backend.entity.Coordinator;
import es.grupo8.backend.entity.CoordinatorId;

@Repository
public interface CoordinatorRepository extends JpaRepository<Coordinator, CoordinatorId> {

}