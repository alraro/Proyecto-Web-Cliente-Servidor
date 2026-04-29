package es.grupo8.backend.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.entity.Volunteer;
import es.grupo8.backend.entity.VolunteerShift;

public interface VolunteerRepository extends JpaRepository<Volunteer, Integer> {

    @Query("SELECT u FROM UserEntity u WHERE u.id = :userId")
    Optional<UserEntity> findUserById(@Param("userId") Integer userId);

    List<VolunteerShift> findByIdIdVolunteerAndIdIdCampaignAndIdIdStoreAndIdShiftDay(
            Integer idVolunteer, Integer idCampaign, Integer idStore, LocalDate shiftDay);

    List<VolunteerShift> findByIdIdVolunteerAndIdIdCampaignAndIdShiftDay(
            Integer idVolunteer, Integer idCampaign, LocalDate shiftDay);

    long countByIdIdCampaignAndIdIdStoreAndIdShiftDayAndIdStartTime(
            Integer idCampaign, Integer idStore, LocalDate shiftDay, LocalTime startTime);

    List<VolunteerShift> findByIdIdCampaignAndIdIdStoreAndIdShiftDay(
            Integer idCampaign, Integer idStore, LocalDate shiftDay);
}