package es.grupo8.backend.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.grupo8.backend.entity.PartnerEntity;
import es.grupo8.backend.entity.VolunteerShift;
import es.grupo8.backend.entity.VolunteerShiftId;

@Repository
public interface VolunteerShiftRepository extends JpaRepository<VolunteerShift, VolunteerShiftId> {

    @Query("SELECT vs FROM VolunteerShift vs " +
           "WHERE vs.id.idCampaign = :campaignId " +
           "AND vs.id.idStore = :storeId " +
           "AND vs.id.shiftDay = :day " +
           "AND vs.id.startTime = :startTime")
    List<VolunteerShift> findByShift(
            @Param("campaignId") Integer campaignId,
            @Param("storeId") Integer storeId,
            @Param("day") LocalDate day,
            @Param("startTime") LocalTime startTime);

    @Query("SELECT COUNT(vs) FROM VolunteerShift vs " +
           "WHERE vs.id.idCampaign = :campaignId " +
           "AND vs.id.idStore = :storeId " +
           "AND vs.id.shiftDay = :day " +
           "AND vs.id.startTime = :startTime")
    long countByShift(
            @Param("campaignId") Integer campaignId,
            @Param("storeId") Integer storeId,
            @Param("day") LocalDate day,
            @Param("startTime") LocalTime startTime);

    @Query("SELECT vs FROM VolunteerShift vs " +
           "WHERE vs.id.idVolunteer = :volunteerId " +
           "AND vs.id.shiftDay = :day " +
           "AND vs.id.startTime < :endTime " +
           "AND vs.endTime > :startTime")
    List<VolunteerShift> findOverlappingForVolunteer(
            @Param("volunteerId") Integer volunteerId,
            @Param("day") LocalDate day,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    @Query("SELECT DISTINCT v.idPartnerEntity " +
           "FROM VolunteerShift vs " +
           "JOIN vs.idVolunteer v " +
           "WHERE vs.id.idCampaign = :campaignId " +
           "AND v.idPartnerEntity IS NOT NULL")
    List<PartnerEntity> findEntitiesWithVolunteersInCampaign(@Param("campaignId") Integer campaignId);

    @Query("SELECT COUNT(vs) " +
           "FROM VolunteerShift vs " +
           "JOIN vs.idVolunteer v " +
           "WHERE vs.id.idCampaign = :campaignId " +
           "AND v.idPartnerEntity.id = :entityId")
    Long countVolunteersInCampaignByEntity(
            @Param("campaignId") Integer campaignId,
            @Param("entityId") Integer entityId);
}
