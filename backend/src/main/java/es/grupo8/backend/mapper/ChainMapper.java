package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.ChainResponseDto;
import es.grupo8.backend.entity.ChainEntity;
import org.springframework.stereotype.Component;

@Component
public class ChainMapper extends MapperDTO<ChainResponseDto, ChainEntity> {

    @Override
    public ChainResponseDto toDTO(ChainEntity entity) {
        if (entity == null) return null;

        return new ChainResponseDto(
                entity.getIdChain(),
                entity.getName(),
                entity.getCode(),
                entity.getParticipation()
        );
    }
}
