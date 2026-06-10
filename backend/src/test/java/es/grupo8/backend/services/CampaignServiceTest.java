package es.grupo8.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.CampaignTypeRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.dto.CampaignRequestDto;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.CampaignType;
import es.grupo8.backend.mapper.CampaignMapper;
import es.grupo8.backend.mapper.CampaignTypeMapper;

/**
 * Unit tests for {@link CampaignService}: DTO mapping, duplicate-name detection, and
 * not-found exception paths. No Spring context or database required.
 */
@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock CampaignRepository campaignRepository;
    @Mock CampaignTypeRepository campaignTypeRepository;
    @Mock CoordinatorRepository coordinatorRepository;
    @Mock CaptainRepository captainRepository;
    @Mock CampaignStoreRepository campaignStoreRepository;
    @Mock CampaignMapper campaignMapper;
    @Mock CampaignTypeMapper campaignTypeMapper;

    @InjectMocks CampaignService campaignService;

    /** getCampaignById returns a populated Optional when the campaign exists. */
    @Test
    void getCampaignById_exists_returnsDto() {
        Campaign campaign = new Campaign();
        CampaignDTO dto = new CampaignDTO();
        dto.setId(1);
        dto.setName("Test Campaign");

        when(campaignRepository.findById(1)).thenReturn(Optional.of(campaign));
        when(campaignMapper.toDTO(campaign)).thenReturn(dto);

        Optional<CampaignDTO> result = campaignService.getCampaignById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("Test Campaign", result.get().getName());
    }

    /** getCampaignById returns an empty Optional when no campaign matches the id. */
    @Test
    void getCampaignById_notExists_returnsEmpty() {
        when(campaignRepository.findById(99)).thenReturn(Optional.empty());

        Optional<CampaignDTO> result = campaignService.getCampaignById(99);

        assertFalse(result.isPresent());
    }

    /** createCampaign throws IllegalStateException when another campaign shares the same name. */
    @Test
    void createCampaign_duplicateName_throwsIllegalStateException() {
        CampaignRequestDto req = new CampaignRequestDto();
        req.setName("Existing Campaign");
        req.setTypeId(1);
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusDays(30));

        CampaignType type = new CampaignType();
        type.setId(1);

        when(campaignTypeRepository.findById(1)).thenReturn(Optional.of(type));
        when(campaignRepository.existsByName("Existing Campaign")).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> campaignService.createCampaign(1, req));
    }

    /** deleteCampaign throws NoSuchElementException when the campaign id does not exist. */
    @Test
    void deleteCampaign_notFound_throwsNoSuchElementException() {
        when(campaignRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> campaignService.deleteCampaign(1, 99));
    }
}
