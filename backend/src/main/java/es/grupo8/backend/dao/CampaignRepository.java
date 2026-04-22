package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.Campaign;

public interface CampaignRepository extends JpaRepository<Campaign, Integer> {
}