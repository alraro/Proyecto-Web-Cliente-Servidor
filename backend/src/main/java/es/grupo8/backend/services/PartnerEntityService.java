package es.grupo8.backend.services;

import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.entity.PartnerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartnerEntityService {

    @Autowired
    private PartnerEntityRepository partnerEntityRepository;

    /**
     * Obtiene todas las entidades partner y las convierte a DTOs.
     */
    public List<PartnerEntityResponseDto> getAllPartnerEntities() {
        return partnerEntityRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Convierte una entidad JPA a DTO de respuesta.
     */
    private PartnerEntityResponseDto toDto(PartnerEntity entity) {
        return new PartnerEntityResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                entity.getPhone()
        );
    }
}
