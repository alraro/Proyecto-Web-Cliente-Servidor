package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.StoreDTO;
import es.grupo8.backend.entity.ChainEntity;
import es.grupo8.backend.entity.Locality;
import es.grupo8.backend.entity.PostalCode;
import org.springframework.stereotype.Component;

// Recorre el código postal → la localidad → la zona geográfica para completar los campos calculados.

@Component
public class StoreMapper extends MapperDTO<StoreDTO, es.grupo8.backend.entity.Store> {

    @Override
    public StoreDTO toDTO(es.grupo8.backend.entity.Store store) {
        StoreDTO dto = new StoreDTO();
        dto.setId(store.getId());
        dto.setName(store.getName());
        dto.setAddress(store.getAddress());

        PostalCode pc = store.getPostalCode();
        if (pc != null) {
            dto.setPostalCode(pc.getPostalCode());
            Locality locality = pc.getIdLocality();
            if (locality != null) {
                dto.setLocality(locality.getName());
                dto.setLocalityId(locality.getId());
                if (locality.getIdZone() != null) {
                    dto.setZone(locality.getIdZone().getName());
                    dto.setZoneId(locality.getIdZone().getId());
                }
            }
        }

        ChainEntity chain = store.getIdChain();
        if (chain != null) {
            dto.setChainId(chain.getIdChain());
            dto.setChainName(chain.getName());
        }

        return dto;
    }
}