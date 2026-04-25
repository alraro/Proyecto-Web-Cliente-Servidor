package es.grupo8.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.grupo8.backend.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Integer> {

    List<Store> findByIdChain_IdChain(Integer chainId);

    @Query("SELECT s FROM Store s WHERE s.postalCode.idLocality.id = :localityId")
    List<Store> findByLocalityId(@Param("localityId") Integer localityId);

    @Query("SELECT s FROM Store s WHERE s.postalCode.idLocality.idZone.id = :zoneId")
    List<Store> findByZoneId(@Param("zoneId") Integer zoneId);

    @Query("SELECT s FROM Store s WHERE s.idChain.idChain = :chainId AND s.postalCode.idLocality.id = :localityId")
    List<Store> findByChainAndLocality(@Param("chainId") Integer chainId,
                                       @Param("localityId") Integer localityId);

    @Query("SELECT s FROM Store s WHERE s.idChain.idChain = :chainId AND s.postalCode.idLocality.idZone.id = :zoneId")
    List<Store> findByChainAndZone(@Param("chainId") Integer chainId,
                                   @Param("zoneId") Integer zoneId);

    /* Verificar si un usuario es Responsable de una tienda concreta */
    boolean existsByIdAndIdResponsible_IdUser(Integer storeId, Integer userId);
}