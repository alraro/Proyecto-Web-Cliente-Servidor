package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.GeographicZone;

public interface GeographicZoneRepository extends JpaRepository<GeographicZone, Integer> {
}