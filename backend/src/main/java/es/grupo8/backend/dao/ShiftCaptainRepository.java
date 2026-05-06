package es.grupo8.backend.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.grupo8.backend.entity.ShiftCaptain;
import es.grupo8.backend.entity.ShiftCaptainId;

@Repository
public interface ShiftCaptainRepository extends JpaRepository<ShiftCaptain, ShiftCaptainId> {

    List<ShiftCaptain> findByShift_Id(Integer shiftId);

    @Query("SELECT sc FROM ShiftCaptain sc " +
           "WHERE sc.user.idUser = :userId " +
           "AND sc.shift.shiftDay = :day " +
           "AND sc.shift.startTime < :endTime " +
           "AND sc.shift.endTime > :startTime " +
           "AND sc.shift.id <> :excludeShiftId")
    List<ShiftCaptain> findOverlappingForCaptain(
            @Param("userId") Integer userId,
            @Param("day") LocalDate day,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeShiftId") Integer excludeShiftId);
}
