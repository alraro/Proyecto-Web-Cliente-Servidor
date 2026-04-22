package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.Captain;
import es.grupo8.backend.entity.CaptainId;

public interface CaptainRepository extends JpaRepository<Captain, CaptainId> {
}