package es.grupo8.backend.dao;
 
import java.util.List;
 
import org.springframework.data.jpa.repository.JpaRepository;
 
import es.grupo8.backend.entity.CampaignStore;
import es.grupo8.backend.entity.CampaignStoreId;
 
public interface CampaignStoreRepository extends JpaRepository<CampaignStore, CampaignStoreId> {
 
    /** Devuelve todas las CampaignStore asociadas a una tienda concreta */
    List<CampaignStore> findByIdStore_Id(Integer storeId);
}