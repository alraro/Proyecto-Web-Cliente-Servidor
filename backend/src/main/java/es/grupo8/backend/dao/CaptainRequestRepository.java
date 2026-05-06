package es.grupo8.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.grupo8.backend.entity.CaptainRequest;

@Repository
public interface CaptainRequestRepository extends JpaRepository<CaptainRequest, Integer> {

    List<CaptainRequest> findByStatus(String status);

    List<CaptainRequest> findByIdCampaign_IdAndStatus(Integer campaignId, String status);

    boolean existsByEmailAndStatus(String email, String status);
}
