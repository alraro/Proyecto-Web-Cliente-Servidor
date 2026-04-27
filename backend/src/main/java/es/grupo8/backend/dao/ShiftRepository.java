package es.grupo8.backend.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.grupo8.backend.entity.Shift;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Integer> {

    // Find all shifts for a specific campaign
    @Query("SELECT s FROM Shift s WHERE s.idCampaign.id = :campaignId")
    List<Shift> findByIdCampaign(@Param("campaignId") Integer idCampaign);

    // Find all shifts for a specific store
    @Query("SELECT s FROM Shift s WHERE s.idStore.id = :storeId")
    List<Shift> findByIdStore(@Param("storeId") Integer idStore);

    // Find shifts for a campaign and store
    @Query("SELECT s FROM Shift s WHERE s.idCampaign.id = :campaignId AND s.idStore.id = :storeId")
    List<Shift> findByCampaignAndStore(
            @Param("campaignId") Integer campaignId,
            @Param("storeId") Integer storeId);

    // Find shifts by day
    @Query("SELECT s FROM Shift s WHERE s.shiftDay = :shiftDay")
    List<Shift> findByShiftDay(@Param("shiftDay") LocalDate shiftDay);

    // Find shifts for a campaign within a date range
    @Query("SELECT s FROM Shift s WHERE s.idCampaign.id = :campaignId AND s.shiftDay BETWEEN :startDate AND :endDate")
    List<Shift> findByCampaignAndDateRange(
            @Param("campaignId") Integer campaignId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}