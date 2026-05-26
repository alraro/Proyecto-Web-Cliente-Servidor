package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.ChainDTO;
import es.grupo8.backend.entity.ChainEntity;
import org.springframework.stereotype.Component;

@Component
public class ChainMapper extends MapperDTO<ChainDTO, ChainEntity> {

    @Override
    public ChainDTO toDTO(ChainEntity entity) {
        ChainDTO dto = new ChainDTO();
        dto.setId(entity.getIdChain());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setParticipation(entity.getParticipation());
        return dto;
    }
}