package es.grupo8.backend.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.entity.Volunteer;

public interface VolunteerRepository extends JpaRepository<Volunteer, Integer> {

    @Query("SELECT u FROM UserEntity u WHERE u.id = :userId")
    Optional<UserEntity> findUserById(@Param("userId") Integer userId);

}