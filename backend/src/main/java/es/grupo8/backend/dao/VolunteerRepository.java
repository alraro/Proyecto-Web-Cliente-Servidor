package es.grupo8.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.grupo8.backend.entity.Volunteer;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Integer> {

    List<Volunteer> findByIdPartnerEntity_Id(Integer partnerEntityId);

    List<Volunteer> findByIdPartnerEntityIsNull();

    List<Volunteer> findAllByOrderByNameAsc();
}
