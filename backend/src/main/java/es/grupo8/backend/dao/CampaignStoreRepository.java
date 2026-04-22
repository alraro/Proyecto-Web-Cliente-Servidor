package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.CampaignStore;
import es.grupo8.backend.entity.CampaignStoreId;

public interface CampaignStoreRepository extends JpaRepository<CampaignStore, CampaignStoreId> {
}