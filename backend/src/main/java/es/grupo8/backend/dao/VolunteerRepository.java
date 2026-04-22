package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.Volunteer;

public interface VolunteerRepository extends JpaRepository<Volunteer, Integer> {
}