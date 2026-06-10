/**
 * Pruebas unitarias del servicio del panel del coordinador.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 85%
 * - IA Generativa: 15%
 */
package es.grupo8.backend.services;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CaptainRequestRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dao.VolunteerRepository;
import es.grupo8.backend.dao.VolunteerShiftRepository;
import es.grupo8.backend.dto.StoreDTO;
import es.grupo8.backend.dto.UserDTO;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.mapper.CampaignEntityMapper;
import es.grupo8.backend.mapper.CampaignMapper;
import es.grupo8.backend.mapper.PartnerEntityMapper;
import es.grupo8.backend.mapper.StoreMapper;
import es.grupo8.backend.mapper.UserMapper;
import es.grupo8.backend.mapper.VolunteerMapper;

/**
 * Verifies that getCaptains and getMyStores resolve their data through JPQL repository
 * methods instead of loading rows and mapping/filtering them in memory.
 */
@ExtendWith(MockitoExtension.class)
class CoordinatorDashboardServiceTest {

    @Mock CoordinatorRepository coordinatorRepository;
    @Mock CaptainRepository captainRepository;
    @Mock CaptainRequestRepository captainRequestRepository;
    @Mock CampaignStoreRepository campaignStoreRepository;
    @Mock UserRepository userRepository;
    @Mock VolunteerRepository volunteerRepository;
    @Mock VolunteerShiftRepository volunteerShiftRepository;
    @Mock PartnerEntityRepository partnerEntityRepository;
    @Mock CampaignMapper campaignMapper;
    @Mock StoreMapper storeMapper;
    @Mock VolunteerMapper volunteerMapper;
    @Mock UserMapper userMapper;
    @Mock PartnerEntityMapper partnerEntityMapper;
    @Mock CampaignEntityMapper campaignEntityMapper;

    @InjectMocks CoordinatorDashboardService service;

    /** getCaptains resolves campaign captains through the JPQL repo method. */
    @Test
    void getCaptains_usesJpql() {
        List<UserEntity> entities = new ArrayList<>();
        List<UserDTO> dtos = new ArrayList<>();
        when(captainRepository.findUsersByCampaignId(1)).thenReturn(entities);
        when(userMapper.toDTOList(entities)).thenReturn(dtos);

        List<UserDTO> result = service.getCaptains(1);

        assertSame(dtos, result);
        verify(captainRepository).findUsersByCampaignId(1);
        verify(captainRepository, never()).findByIdIdCampaign(1);
    }

    /** getCaptains requires a campaign id. */
    @Test
    void getCaptains_nullCampaign_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getCaptains(null));
    }

    /** getMyStores resolves campaign stores through the JPQL repo method. */
    @Test
    void getMyStores_usesJpql() {
        List<Store> stores = new ArrayList<>();
        List<StoreDTO> dtos = new ArrayList<>();
        when(campaignStoreRepository.findStoresByCampaignId(3)).thenReturn(stores);
        when(storeMapper.toDTOList(stores)).thenReturn(dtos);

        List<StoreDTO> result = service.getMyStores(3);

        assertSame(dtos, result);
        verify(campaignStoreRepository).findStoresByCampaignId(3);
        verify(campaignStoreRepository, never()).findByIdCampaign_Id(3);
    }

    /** getMyStores requires a campaign id. */
    @Test
    void getMyStores_nullCampaign_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getMyStores(null));
    }
}
