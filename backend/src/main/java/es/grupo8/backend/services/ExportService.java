package es.grupo8.backend.services;

import java.util.List;

import org.springframework.stereotype.Service;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.ChainRepository;
import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dto.ExportCampaignDTO;
import es.grupo8.backend.dto.ExportChainDTO;
import es.grupo8.backend.dto.ExportPartnerDTO;
import es.grupo8.backend.dto.ExportStoreDTO;
import es.grupo8.backend.dto.ExportUserDTO;
import es.grupo8.backend.mapper.ExportCampaignMapper;
import es.grupo8.backend.mapper.ExportChainMapper;
import es.grupo8.backend.mapper.ExportPartnerMapper;
import es.grupo8.backend.mapper.ExportStoreMapper;
import es.grupo8.backend.mapper.ExportUserMapper;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ExportService {

    private final StoreRepository storeRepository;
    private final ChainRepository chainRepository;
    private final CampaignRepository campaignRepository;
    private final PartnerEntityRepository partnerEntityRepository;
    private final UserRepository userRepository;

    private final ExportStoreMapper exportStoreMapper;
    private final ExportChainMapper exportChainMapper;
    private final ExportCampaignMapper exportCampaignMapper;
    private final ExportPartnerMapper exportPartnerMapper;
    private final ExportUserMapper exportUserMapper;



    public List<ExportStoreDTO>    getStores()    { 
        return exportStoreMapper.toDTOList(storeRepository.findAll()); 
    }

    public List<ExportChainDTO>    getChains()    { 
        return exportChainMapper.toDTOList(chainRepository.findAll()); 
    }

    public List<ExportCampaignDTO> getCampaigns() { 
        return exportCampaignMapper.toDTOList(campaignRepository.findAll()); 
    }

    public List<ExportPartnerDTO>  getPartners()  { 
        return exportPartnerMapper.toDTOList(partnerEntityRepository.findAll()); 
    }

    public List<ExportUserDTO>     getUsers()     { 
        return exportUserMapper.toDTOList(userRepository.findAll()); 
    }

}