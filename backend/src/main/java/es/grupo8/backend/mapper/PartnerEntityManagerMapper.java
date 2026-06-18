/**
 * Autores:
 * - Alfonso Ramos Rojas: 100%
 */
package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.PartnerEntityManagerResponseDto;
import es.grupo8.backend.entity.PartnerEntity;
import es.grupo8.backend.entity.PartnerEntityManager;
import es.grupo8.backend.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class PartnerEntityManagerMapper extends MapperDTO<PartnerEntityManagerResponseDto, PartnerEntityManager> {

    @Override
    public PartnerEntityManagerResponseDto toDTO(PartnerEntityManager manager) {
        if (manager == null) return null;

        UserEntity user = manager.getUserAccounts();
        PartnerEntity partnerEntity = manager.getIdPartnerEntity();

        return new PartnerEntityManagerResponseDto(
                manager.getId(),
                user != null ? user.getName() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getPhone() : null,
                user != null ? user.getAddress() : null,
                user != null ? user.getPostalCode() : null,
                partnerEntity != null ? partnerEntity.getId() : null,
                partnerEntity != null ? partnerEntity.getName() : null
        );
    }
}
