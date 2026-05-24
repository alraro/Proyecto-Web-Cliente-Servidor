package es.grupo8.backend.mapper;

import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.ExportChainDTO;
import es.grupo8.backend.entity.ChainEntity;

@Component
public class ExportChainMapper extends MapperDTO<ExportChainDTO, ChainEntity> {

    @Override
    public ExportChainDTO toDTO (ChainEntity c) {
        if(c == null) return null;

        ExportChainDTO dto = new ExportChainDTO();

        dto.setId(c.getIdChain());
        dto.setName(c.getName());
        dto.setCode(c.getCode());
        dto.setParticipation(Boolean.TRUE.equals(c.getParticipation()) ? "Sí" : "No");

        return dto;
    }
}