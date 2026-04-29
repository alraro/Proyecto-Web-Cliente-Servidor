package es.grupo8.backend.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.VolunteerShift;
import es.grupo8.backend.entity.VolunteerShiftId;

public interface VolunteerShiftRepository extends JpaRepository<VolunteerShift, VolunteerShiftId> {

    List<VolunteerShift> findByIdIdVolunteerAndIdIdCampaignAndIdIdStoreAndIdShiftDay(
            Integer idVolunteer, Integer idCampaign, Integer idStore, LocalDate shiftDay);

    List<VolunteerShift> findByIdIdVolunteerAndIdIdCampaignAndIdShiftDay(
            Integer idVolunteer, Integer idCampaign, LocalDate shiftDay);

    long countByIdIdCampaignAndIdIdStoreAndIdShiftDayAndIdStartTime(
            Integer idCampaign, Integer idStore, LocalDate shiftDay, LocalTime startTime);

    List<VolunteerShift> findByIdIdCampaignAndIdIdStoreAndIdShiftDay(
            Integer idCampaign, Integer idStore, LocalDate shiftDay);
}