package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.CampaignType;

public interface CampaignTypeRepository extends JpaRepository<CampaignType, Integer> {
}