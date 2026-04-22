package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.VolunteerShift;
import es.grupo8.backend.entity.VolunteerShiftId;

public interface VolunteerShiftRepository extends JpaRepository<VolunteerShift, VolunteerShiftId> {
}