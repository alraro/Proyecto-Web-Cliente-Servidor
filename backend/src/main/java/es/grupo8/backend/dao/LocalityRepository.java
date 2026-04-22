package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.Locality;

public interface LocalityRepository extends JpaRepository<Locality, Integer> {
}