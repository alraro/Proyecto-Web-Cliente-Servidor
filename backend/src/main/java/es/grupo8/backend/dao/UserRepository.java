package es.grupo8.backend.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
