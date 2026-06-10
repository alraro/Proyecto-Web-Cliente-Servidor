/**
 * Pruebas unitarias del servicio de asignación de coordinadores y capitanes.
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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dto.CampaignAssignmentsDTO;
import es.grupo8.backend.dto.UserDTO;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.mapper.CampaignAssignmentsMapper;
import es.grupo8.backend.mapper.CampaignMapper;
import es.grupo8.backend.mapper.UserMapper;

/**
 * Verifies the JPQL-based refactor: the service must delegate the look-ups to the
 * repository query methods and only apply the mapper, never filtering in memory.
 */
@ExtendWith(MockitoExtension.class)
class CampaignAssignmentServiceTest {

    @Mock CampaignRepository campaignRepository;
    @Mock UserRepository userRepository;
    @Mock CoordinatorRepository coordinatorRepository;
    @Mock CaptainRepository captainRepository;
    @Mock CampaignMapper campaignMapper;
    @Mock UserMapper userMapper;
    @Mock CampaignAssignmentsMapper campaignAssignmentsMapper;

    @InjectMocks CampaignAssignmentService service;

    /** getCampaignAssignments queries coordinators/captains via JPQL and maps them. */
    @Test
    void getCampaignAssignments_usesJpqlAndMaps() {
        Campaign campaign = new Campaign();
        campaign.setId(1);
        List<UserEntity> coordEntities = List.of(new UserEntity());
        List<UserEntity> capEntities = List.of(new UserEntity(), new UserEntity());
        List<UserDTO> coordDtos = List.of(new UserDTO());
        List<UserDTO> capDtos = List.of(new UserDTO(), new UserDTO());
        CampaignAssignmentsDTO expected = new CampaignAssignmentsDTO();

        when(campaignRepository.findById(1)).thenReturn(Optional.of(campaign));
        when(coordinatorRepository.findUsersByCampaignId(1)).thenReturn(coordEntities);
        when(captainRepository.findUsersByCampaignId(1)).thenReturn(capEntities);
        when(userMapper.toDTOList(coordEntities)).thenReturn(coordDtos);
        when(userMapper.toDTOList(capEntities)).thenReturn(capDtos);
        when(campaignAssignmentsMapper.toDTO(campaign, coordDtos, capDtos)).thenReturn(expected);

        CampaignAssignmentsDTO result = service.getCampaignAssignments(99, 1);

        assertSame(expected, result);
        verify(coordinatorRepository).findUsersByCampaignId(1);
        verify(captainRepository).findUsersByCampaignId(1);
    }

    /** getCampaignAssignments throws when the campaign does not exist. */
    @Test
    void getCampaignAssignments_campaignNotFound_throws() {
        when(campaignRepository.findById(7)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.getCampaignAssignments(99, 7));
    }

    /** getAvailableUsers(COORDINATOR) delegates to the JPQL available-coordinators query. */
    @Test
    void getAvailableUsers_coordinator_usesJpql() {
        List<UserEntity> entities = List.of(new UserEntity());
        List<UserDTO> dtos = List.of(new UserDTO());
        when(campaignRepository.existsById(1)).thenReturn(true);
        when(userRepository.findAvailableCoordinators(1)).thenReturn(entities);
        when(userMapper.toDTOList(entities)).thenReturn(dtos);

        List<UserDTO> result = service.getAvailableUsers(99, 1, "COORDINATOR");

        assertSame(dtos, result);
        verify(userRepository).findAvailableCoordinators(1);
        verify(userRepository, never()).findAllCoordinators();
    }

    /** getAvailableUsers(CAPTAIN) delegates to the JPQL available-captains query. */
    @Test
    void getAvailableUsers_captain_usesJpql() {
        List<UserEntity> entities = List.of(new UserEntity());
        List<UserDTO> dtos = List.of(new UserDTO());
        when(campaignRepository.existsById(2)).thenReturn(true);
        when(userRepository.findAvailableCaptains(2)).thenReturn(entities);
        when(userMapper.toDTOList(entities)).thenReturn(dtos);

        List<UserDTO> result = service.getAvailableUsers(99, 2, "CAPTAIN");

        assertSame(dtos, result);
        verify(userRepository).findAvailableCaptains(2);
        verify(userRepository, never()).findAllCaptains();
    }

    /** getAvailableUsers rejects an invalid role. */
    @Test
    void getAvailableUsers_invalidRole_throws() {
        when(campaignRepository.existsById(1)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.getAvailableUsers(99, 1, "FOO"));
    }

    /** getAvailableUsers throws when the campaign does not exist. */
    @Test
    void getAvailableUsers_campaignNotFound_throws() {
        when(campaignRepository.existsById(5)).thenReturn(false);
        assertThrows(NoSuchElementException.class, () -> service.getAvailableUsers(99, 5, "COORDINATOR"));
    }
}
