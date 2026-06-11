package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.StoreResponseDto;
import es.grupo8.backend.entity.ChainEntity;
import es.grupo8.backend.entity.Locality;
import es.grupo8.backend.entity.PostalCode;
import org.springframework.stereotype.Component;

@Component
public class StoreMapper extends MapperDTO<StoreResponseDto, es.grupo8.backend.entity.Store> {

    @Override
    public StoreResponseDto toDTO(es.grupo8.backend.entity.Store store) {
        if (store == null) return null;

        String postalCodeValue = null;
        String localityName = null;
        Integer localityId = null;
        String zoneName = null;
        Integer zoneId = null;
        Integer chainId = null;
        String chainName = null;

        PostalCode postalCode = store.getPostalCode();
        if (postalCode != null) {
            postalCodeValue = postalCode.getPostalCode();
            Locality locality = postalCode.getIdLocality();
            if (locality != null) {
                localityName = locality.getName();
                localityId = locality.getId();
                if (locality.getIdZone() != null) {
                    zoneName = locality.getIdZone().getName();
                    zoneId = locality.getIdZone().getId();
                }
            }
        }

        ChainEntity chain = store.getIdChain();
        if (chain != null) {
            chainId = chain.getIdChain();
            chainName = chain.getName();
        }

        return new StoreResponseDto(
                store.getId(),
                store.getName(),
                store.getAddress(),
                postalCodeValue,
                chainId,
                localityName,
                localityId,
                zoneName,
                zoneId,
                chainName
        );
    }
}
