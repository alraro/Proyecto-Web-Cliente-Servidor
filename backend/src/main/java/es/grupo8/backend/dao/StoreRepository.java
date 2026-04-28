package es.grupo8.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.grupo8.backend.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Integer> {

    List<Store> findAllByOrderByIdAsc();

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

    /* Check if a user is the assigned manager of a specific store */
    boolean existsByIdAndIdResponsible_IdUser(Integer storeId, Integer userId);

    /* Find the store assigned to a responsible user (used at login) */
    java.util.Optional<Store> findByIdResponsible_IdUser(Integer userId);

    /* Check if a user is responsible for any store */
    boolean existsByIdResponsible_IdUser(Integer userId);
}