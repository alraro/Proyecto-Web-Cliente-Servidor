package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.District;

public interface DistrictRepository extends JpaRepository<District, Integer> {
}