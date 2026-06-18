/**
 * Repositorio JPA de turnos de voluntarios.
 *
 * Autores:
 * - Alejandro Calvo Aguilar: 50%
 * - Fernando Luis Pinilla Molina: 20%
 * - Alfonso Ramos Rojas: 15%
 * - IA Generativa: 15%
 */
package es.grupo8.backend.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.PartnerEntity;
import es.grupo8.backend.entity.VolunteerShift;
import es.grupo8.backend.entity.VolunteerShiftId;

@Repository
public interface VolunteerShiftRepository extends JpaRepository<VolunteerShift, VolunteerShiftId> {

    // Shifts of a campaign, filtered by store if storeId is given (null = all stores)
    @Query("SELECT vs FROM VolunteerShift vs " +
           "WHERE vs.id.idCampaign = :campaignId " +
           "AND (:storeId IS NULL OR vs.id.idStore = :storeId)")
    List<VolunteerShift> findByCampaignAndOptionalStore(
            @Param("campaignId") Integer campaignId,
            @Param("storeId") Integer storeId);

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

    @Query("SELECT vs.id.idStore, vs.id.shiftDay, vs.id.startTime, COUNT(vs) " +
           "FROM VolunteerShift vs " +
           "WHERE vs.id.idCampaign = :campaignId " +
           "GROUP BY vs.id.idStore, vs.id.shiftDay, vs.id.startTime")
    List<Object[]> countVolunteersPerShiftInCampaign(@Param("campaignId") Integer campaignId);

    @Query("SELECT DISTINCT vs.campaignStores.idCampaign "
         + "FROM VolunteerShift vs "
         + "JOIN vs.idVolunteer v "
         + "WHERE v.idPartnerEntity.id = :entityId")
    List<Campaign> findCampaignsByEntityId(@Param("entityId") Integer entityId);
}
