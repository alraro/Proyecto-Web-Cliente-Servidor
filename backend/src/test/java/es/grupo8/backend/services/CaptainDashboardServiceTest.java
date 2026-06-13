/**
 * Pruebas unitarias del servicio del panel del capitán.
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
import es.grupo8.backend.dao.IncidentRepository;
import es.grupo8.backend.dao.ShiftRepository;
import es.grupo8.backend.dao.VolunteerShiftRepository;
import es.grupo8.backend.dto.StoreResponseDto;
import es.grupo8.backend.dto.VolunteerShiftDTO;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.entity.VolunteerShift;
import es.grupo8.backend.mapper.CampaignMapper;
import es.grupo8.backend.mapper.IncidentMapper;
import es.grupo8.backend.mapper.ShiftMapper;
import es.grupo8.backend.mapper.StoreMapper;
import es.grupo8.backend.mapper.VolunteerShiftMapper;

@ExtendWith(MockitoExtension.class)
class CaptainDashboardServiceTest {

    @Mock CaptainRepository captainRepository;
    @Mock CampaignStoreRepository campaignStoreRepository;
    @Mock ShiftRepository shiftRepository;
    @Mock VolunteerShiftRepository volunteerShiftRepository;
    @Mock IncidentRepository incidentRepository;
    @Mock CampaignMapper campaignMapper;
    @Mock StoreMapper storeMapper;
    @Mock ShiftMapper shiftMapper;
    @Mock VolunteerShiftMapper volunteerShiftMapper;
    @Mock IncidentMapper incidentMapper;

    @InjectMocks CaptainDashboardService service;

    @Test
    void getVolunteerShifts_withStore_usesJpql() {
        List<VolunteerShift> shifts = new ArrayList<>();
        List<VolunteerShiftDTO> dtos = new ArrayList<>();
        when(volunteerShiftRepository.findByCampaignAndOptionalStore(1, 2)).thenReturn(shifts);
        when(volunteerShiftMapper.toDTOList(shifts)).thenReturn(dtos);

        List<VolunteerShiftDTO> result = service.getVolunteerShifts(10, 1, 2);

        assertSame(dtos, result);
        verify(volunteerShiftRepository).findByCampaignAndOptionalStore(1, 2);
        verify(volunteerShiftRepository, never()).findAll();
    }

    @Test
    void getVolunteerShifts_nullStore_passesNull() {
        List<VolunteerShift> shifts = new ArrayList<>();
        List<VolunteerShiftDTO> dtos = new ArrayList<>();
        when(volunteerShiftRepository.findByCampaignAndOptionalStore(1, null)).thenReturn(shifts);
        when(volunteerShiftMapper.toDTOList(shifts)).thenReturn(dtos);

        List<VolunteerShiftDTO> result = service.getVolunteerShifts(10, 1, null);

        assertSame(dtos, result);
        verify(volunteerShiftRepository).findByCampaignAndOptionalStore(1, null);
    }

    @Test
    void getVolunteerShifts_nullCampaign_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getVolunteerShifts(10, null, 2));
    }

    @Test
    void getMyStores_usesJpql() {
        List<Store> stores = new ArrayList<>();
        List<StoreResponseDto> dtos = new ArrayList<>();
        when(campaignStoreRepository.findStoresByCampaignId(5)).thenReturn(stores);
        when(storeMapper.toDTOList(stores)).thenReturn(dtos);

        List<StoreResponseDto> result = service.getMyStores(10, 5);

        assertSame(dtos, result);
        verify(campaignStoreRepository).findStoresByCampaignId(5);
        verify(campaignStoreRepository, never()).findByIdCampaign_Id(5);
    }
}
